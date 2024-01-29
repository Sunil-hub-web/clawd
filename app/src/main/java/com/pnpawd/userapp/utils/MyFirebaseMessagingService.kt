package com.pnpawd.userapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pnpawd.userapp.MainActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.SplashScreen


const val channelId = "notification_channel"
const val channelName = "test_channel"

class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
// Handle incoming message here
//        Log.e("notiication", remoteMessage.notification?.body.toString())
//        if (remoteMessage.notification != null) {
//            showNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
//        }


        generateNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!! )
    }

    private fun generateNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.notification)
                .setAutoCancel(true).setVibrate(
                    longArrayOf(1000, 1000, 1000, 1000)
                ).setOnlyAlertOnce(true).setContentIntent(pendingIntent)


        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder = builder.setContent(getRemoteView(title, body))

            val notificationChannel= NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }


    private fun getRemoteView(title: String, message: String): RemoteViews {
        val remoteViews = RemoteViews("com.pnpawd.userapp", R.layout.notification)
        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(R.id.desc, message)
//        remoteViews.setImageViewResource(R.id.app_logo, R.drawable.notification)
        return remoteViews
    }

//    private fun showNotification(title: String?, body: String?) {
//        Log.e("notiication", title.toString())
//        Log.e("notiication", body.toString())
//
//
//        val intent = Intent(this, SplashScreen::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        Log.e("notiication", "reached here 1")
//
//        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val notificationBuilder = NotificationCompat.Builder(this)
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentTitle(title)
//            .setContentText(body)
//            .setAutoCancel(true)
//            .setSound(soundUri)
//            .setContentIntent(pendingIntent)
//
//        Log.e("notiication", "reached here 2")
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(0, notificationBuilder.build())
//
//        Log.e("notiication", "reached here 4")
//    }
}