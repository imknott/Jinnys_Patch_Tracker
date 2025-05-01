
package com.knottlabs.jinnyspatchtracker

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission

object AlarmManagerHelper {
    private const val REQUEST_CODE_BASE = 1000

    /**
     * Schedules a simple notification reminder that will open the app when clicked
     * @param context Application context
     * @param patchName Name of patch for notification text
     * @param triggerTime When to show the notification (millis since epoch)
     * @param patchId Unique ID for this patch (used for request code)
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun schedulePatchReminder(
        context: Context,
        patchName: String,
        triggerTime: Long,
        patchId: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = createReminderIntent(context, patchName, patchId)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generateRequestCode(patchId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            NotificationHelper.showPermissionWarning(context)
            return
        }

        // Set the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Creates an intent that will trigger a simple notification
     */
    private fun createReminderIntent(context: Context, patchName: String, patchId: Int): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra("patchName", patchName)
            putExtra("patchId", patchId)
        }
    }

    /**
     * Generates unique request codes for each patch
     */
    private fun generateRequestCode(patchId: Int): Int {
        return REQUEST_CODE_BASE + patchId
    }

    /**
     * Cancels a pending reminder for a specific patch
     */
    fun cancelReminder(context: Context, patchId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generateRequestCode(patchId),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}