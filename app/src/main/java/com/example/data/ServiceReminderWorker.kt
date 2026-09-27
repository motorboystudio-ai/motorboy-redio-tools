package com.example.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.domain.ServiceReminderNotificationHelper
import java.util.concurrent.TimeUnit

/**
 * Background check for the oil-change reminder.
 * Runs once a day and notifies ONLY when the service is due or near due,
 * so the user gets reminded even without opening the app.
 */
class ServiceReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = ServiceReminderPreferences.getInstance(applicationContext)
            val reminder = prefs.loadReminder()
            if (reminder.isDue || reminder.isNearDue) {
                ServiceReminderNotificationHelper.sendReminderNotification(
                    applicationContext,
                    reminder
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "service_reminder_daily_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ServiceReminderWorker>(24, TimeUnit.HOURS)
                .addTag(UNIQUE_WORK_NAME)
                .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
