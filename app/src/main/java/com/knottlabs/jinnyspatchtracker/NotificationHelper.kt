package com.knottlabs.jinnyspatchtracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest

object NotificationHelper {
    // Notification channel constants
    private const val CHANNEL_ID = "patch_reminder_channel"
    private const val CHANNEL_NAME = "Patch Reminder"
    private const val CHANNEL_DESC = "Reminds you when it's time to change your patch"

    // Notification IDs
    private const val BASE_NOTIFICATION_ID = 100
    private const val PERMISSION_NOTIFICATION_ID = 99

    /**
     * Creates the notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a simple notification that opens the app when clicked
     * @param context Application context
     * @param patchName Optional patch name to personalize the message
     * @param patchId Used to generate unique notification ID
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSimpleReminder(context: Context, patchName: String? = null, patchId: Int) {
        val contentTitle = patchName?.let {
            "Time to change your $it patch!"
        } ?: "Time to change your patch!"

        // Intent to open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_patch_id", patchId) // Pass patch ID to activity
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            patchId, // Use patchId as request code for uniqueness
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon
            .setContentTitle(contentTitle)
            .setContentText("Tap to open the app")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show notification with unique ID for each patch
        NotificationManagerCompat.from(context).notify(
            BASE_NOTIFICATION_ID + patchId,
            notification
        )
    }

    /**
     * Shows a notification when exact alarm permission is needed (Android 12+)
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showPermissionWarning(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Permission Needed")
            .setContentText("Please enable exact alarms for reminders to work")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            PERMISSION_NOTIFICATION_ID,
            notification
        )
    }

    /**
     * Cancels a specific notification by patch ID
     */
    fun cancelNotification(context: Context, patchId: Int) {
        NotificationManagerCompat.from(context).cancel(BASE_NOTIFICATION_ID + patchId)
    }
}