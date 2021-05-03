package com.sample.vaccineavailability.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.sample.vaccineavailability.BuildConfig
import com.sample.vaccineavailability.R
import java.util.*

object NotificatonHelper {
    private const val CHANNEL_ID_URGENT = BuildConfig.APPLICATION_ID.plus("1001").plus("urgent")
    private const val CHANNEL_ID_DEFAULT = BuildConfig.APPLICATION_ID.plus("1001").plus("default")
    private const val CHANNEL_ID_MED = BuildConfig.APPLICATION_ID.plus("1001").plus("medium")
    private const val CHANNEL_ID_LOW = BuildConfig.APPLICATION_ID.plus("1001").plus("low")
    private const val CHANNEL_NAME = "Notification Monitor"

    private lateinit var mNotification: Notification
    private lateinit var mNotificationBuilder: NotificationCompat.Builder
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var channelId: String

    private fun createChannel(context: Context) {
        sharedPreferences = context.getSharedPreferences("Info", Context.MODE_PRIVATE)!!
        channelId = getChannelId("urgent")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val importance =  NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, CHANNEL_NAME, importance)

            //we can set the color later.
            val color = "#0063d8" //for now blue color

            notificationChannel.importance = importance
            notificationChannel.lightColor = Color.parseColor(color)
            notificationChannel.description = "Slot is available Check now!!"
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }

    private fun getChannelId(importance: String): String {
        return when(importance) {
            "urgent" -> CHANNEL_ID_URGENT
            "high" -> CHANNEL_ID_DEFAULT
            "medium" -> CHANNEL_ID_MED
            "low" -> CHANNEL_ID_LOW
            else -> CHANNEL_ID_DEFAULT
        }
    }

    fun onHandleEvent(title: String, description: String, context: Context) {

        //Create Channel
        createChannel(context)


        val id = getRandomNumberInRange(0, 1000)


        if (title.isNotEmpty()) {

            val notificationManager: NotificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT
            )


            //WAKE SCREEN............................................

            var wakeLock: PowerManager.WakeLock? = null

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isScreenOn = powerManager.isInteractive
            if (!isScreenOn) {

                wakeLock =
                    powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        "EC:Event_Reminder"
                    )
                wakeLock.acquire(10000)

            }


            //END...................................
            mNotificationBuilder = NotificationCompat.Builder(context, channelId)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.parseColor("#0063d8"))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentTitle(title)
                .setContentText(description)


            mNotification = mNotificationBuilder.build()

            notificationManager.notify(id, mNotification)

            wakeLock?.release()


        }
    }

    private fun getRandomNumberInRange(min: Int, max: Int): Int {
        require(min < max) { "max must be greater than min" }

        val r = Random()
        return r.nextInt(max - min + 1) + min
    }
}