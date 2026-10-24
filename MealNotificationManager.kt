package dz.mesrs.webetu.automeals.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MealNotificationManager(private val context: Context) {
    companion object {
        const val CHANNEL_ID_BOOKING = "webetu_booking_channel"
        const val CHANNEL_ID_ALERTS = "webetu_alerts_channel"
        const val WEBETU_PACKAGE_NAME = "app.progres.webetu"
        const val WEBETU_PORTAL_URL = "https://progres.mesrs.dz/webetu/"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bookingChannel = NotificationChannel(CHANNEL_ID_BOOKING, "حجز الوجبات", NotificationManager.IMPORTANCE_HIGH)
            val alertsChannel = NotificationChannel(CHANNEL_ID_ALERTS, "تنبيهات Webetu", NotificationManager.IMPORTANCE_DEFAULT)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(bookingChannel)
            nm.createNotificationChannel(alertsChannel)
        }
    }

    fun notifyBookingConfirmed(mealName: String, targetDate: String, restaurantName: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BOOKING)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("تم تأكيد الحجز بنجاح")
            .setContentText("تم تأكيد حجز $mealName ليوم $targetDate بمطعم $restaurantName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }

    fun notifyUserInterventionRequired(mealName: String, targetDate: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(WEBETU_PACKAGE_NAME)
            ?: Intent(Intent.ACTION_VIEW, Uri.parse(WEBETU_PORTAL_URL))
        val pendingIntent = PendingIntent.getActivity(context, 1, launchIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BOOKING)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("مطلوب تدخل المستخدم لحجز الوجبة")
            .setContentText("حان وقت حجز $mealName ليوم $targetDate. اضغط لفتح Webetu.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(1005, builder.build())
    }
}
