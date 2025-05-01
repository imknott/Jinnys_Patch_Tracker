package com.knottlabs.jinnyspatchtracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        // Optional: Add any constants or helper methods here if needed
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val patchName = intent.getStringExtra("patchName")
        val patchId = intent.getIntExtra("patchId", -1)

        if (patchId != -1) {
            try {
                // Initialize NotificationHelper if needed
                NotificationHelper.createNotificationChannel(context)

                // Show the notification
                NotificationHelper.showSimpleReminder(
                    context = context,
                    patchName = patchName,
                    patchId = patchId
                )
            } catch (e: Exception) {
                // Handle any notification errors
                e.printStackTrace()
            }
        }
    }
}