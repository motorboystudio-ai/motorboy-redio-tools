package com.example.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

data class OilChangeReminder(
    val bikeModel: String = "Honda Wave 110i",
    val lastOilChangeMileage: Int = 12000,
    val currentMileage: Int = 13850,
    val intervalKm: Int = 2000, // Standard interval: 2,000 km (or 1,500 - 3,000 km for scooters/motorcycles)
    val lastChangeDate: String = "2026-08-15",
    val oilBrandGrade: String = "10W-30 JASO MA",
    val notes: String = "เปลี่ยนแหวนรองถ่ายน้ำมันเครื่องด้วย"
) {
    val nextDueMileage: Int
        get() = lastOilChangeMileage + intervalKm

    val kmDrivenSinceLastChange: Int
        get() = (currentMileage - lastOilChangeMileage).coerceAtLeast(0)

    val kmRemaining: Int
        get() = nextDueMileage - currentMileage

    val progressFraction: Float
        get() {
            if (intervalKm <= 0) return 1f
            return (kmDrivenSinceLastChange.toFloat() / intervalKm.toFloat()).coerceIn(0f, 1f)
        }

    val isDue: Boolean
        get() = currentMileage >= nextDueMileage

    val isNearDue: Boolean
        get() = !isDue && kmRemaining <= (intervalKm * 0.2f).toInt() // Within 20% of interval
}

object ServiceReminderNotificationHelper {
    private const val CHANNEL_ID = "service_reminder_channel"
    private const val CHANNEL_NAME = "แจ้งเตือนเปลี่ยนถ่ายน้ำมันเครื่อง & ซ่อมบำรุง"
    private const val NOTIFICATION_ID = 2026

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "แจ้งเตือนเมื่อถึงกำหนดหรือใกล้ถึงกำหนดเปลี่ยนถ่ายน้ำมันเครื่อง"
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendReminderNotification(
        context: Context,
        reminder: OilChangeReminder
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title: String
        val contentText: String

        if (reminder.isDue) {
            val overdueKm = (reminder.currentMileage - reminder.nextDueMileage).coerceAtLeast(0)
            title = "⚠️ ถึงกำหนดเปลี่ยนถ่ายน้ำมันเครื่องแล้ว!"
            contentText = if (overdueKm > 0) {
                "${reminder.bikeModel}: เลขไมล์ปัจจุบัน ${reminder.currentMileage} กม. (เกินกำหนดแล้ว $overdueKm กม.)"
            } else {
                "${reminder.bikeModel}: ถึงระยะกำหนดเปลี่ยนที่เลขไมล์ ${reminder.nextDueMileage} กม. พอดี"
            }
        } else if (reminder.isNearDue) {
            title = "🔔 ใกล้ถึงรอบเปลี่ยนถ่ายน้ำมันเครื่อง"
            contentText = "${reminder.bikeModel}: เหลืออีกเพียง ${reminder.kmRemaining} กม. จะถึงรอบ ${reminder.nextDueMileage} กม."
        } else {
            title = "✅ สภาพน้ำมันเครื่องอยู่ในเกณฑ์ปกติ"
            contentText = "${reminder.bikeModel}: เหลือระยะอีก ${reminder.kmRemaining} กม. (กำหนดถัดไป ${reminder.nextDueMileage} กม.)"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$contentText\n\n- ไมล์เปลี่ยนล่าสุด: ${reminder.lastOilChangeMileage} กม.\n- สเปกน้ำมัน: ${reminder.oilBrandGrade}\n- รอบระยะ: ทุกๆ ${reminder.intervalKm} กม."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Handled when user denied notification permissions
        }
    }
}
