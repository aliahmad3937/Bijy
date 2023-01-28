package com.codecoy.bijy.utils



import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder

import com.codecoy.bijy.MainActivity
import com.codecoy.bijy.R
import com.codecoy.bijy.owner.models.Flag


// Notification ID.
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

// TODO: Step 1.1 extension function to send messages (GIVEN)
/**
 * Builds and delivers the notification.
 *
 * @param messageBody, notification text.
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {

    createChannel(
        applicationContext,
        applicationContext.getString(R.string.breakfast_notification_channel_id),
        applicationContext.getString(R.string.breakfast_notification_channel_name)
    )

    // TODO: Step 1.11 create intent
    var contentIntent = Intent(applicationContext, MainActivity::class.java)
        .putExtra("from", "notification")

    // TODO: Step 1.12 create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

// TODO: Step 2.0 add style
    val eggImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.account
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(eggImage)
        .bigLargeIcon(null)


    // TODO: Step 2.2 add snooze action
//    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
//    val snoozePendingIntent: PendingIntent =
//        PendingIntent.getBroadcast(applicationContext, REQUEST_CODE, snoozeIntent, FLAGS)

    // TODO: Step 1.2 get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        // TODO: Step 1.8 use the new 'breakfast' notification channel
        applicationContext.getString(R.string.breakfast_notification_channel_id)
    )
        // TODO: Step 1.3 set title, text and icon to builder
        .setSmallIcon(R.drawable.account)
        .setContentTitle(applicationContext.getString(R.string.app_name))
        .setContentText(messageBody)
        // TODO: Step 1.13 set content intent
        .setContentIntent(contentPendingIntent)
        // TODO: Step 2.1 add style to builder

        // TODO: Step 2.3 add snooze action
        .setContentIntent(contentPendingIntent)
        // TODO: Step 2.5 set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    // TODO Step 1.4 call notify
    // Deliver the notification
    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.sendNotification(messageBody:  MutableMap<String, String>, applicationContext: Context) {
    Log.d("TAG7", "Message data")
    createChannel(
        applicationContext,
        applicationContext.getString(R.string.breakfast_notification_channel_id),
        applicationContext.getString(R.string.breakfast_notification_channel_name)
    )

//    // TODO: Step 1.11 create intent
//    var contentIntent = Intent(applicationContext, MainActivity::class.java)
//        .putExtra("from", "notification")
//
//    // TODO: Step 1.12 create PendingIntent
//    val contentPendingIntent = PendingIntent.getActivity(
//        applicationContext,
//        NOTIFICATION_ID,
//        contentIntent,
//        PendingIntent.FLAG_IMMUTABLE
//    )


    val flag:Flag = Flag(
        flagID =  messageBody["flagID"].toString(),
        userID =  messageBody["userID"].toString(),
        userName = messageBody["userName"].toString(),
        latitude = messageBody["latitude"]!!.toDouble(),
        longitude = messageBody["longitude"]!!.toDouble(),
        title = messageBody["title"].toString(),
        address = messageBody["address"].toString(),
        flagStatus =messageBody["flagStatus"].toString(),
        userType =messageBody["userType"].toString(),
        date =messageBody["date"].toString(),
        time = messageBody["time"].toString(),
        flagNote = messageBody["flagNote"].toString(),
        data = messageBody["data"].toString()

    )

    val bundle = Bundle()
    bundle.putSerializable("flag",flag)


    val pendingIntent: PendingIntent = NavDeepLinkBuilder(applicationContext)
        .setGraph(com.codecoy.bijy.R.navigation.nav_graph)
        .setDestination(com.codecoy.bijy.R.id.userFlagDetail)
        .setArguments(bundle)
        .createPendingIntent()



// TODO: Step 2.0 add style
    val eggImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.account
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(eggImage)
        .bigLargeIcon(null)


    // TODO: Step 2.2 add snooze action
//    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
//    val snoozePendingIntent: PendingIntent =
//        PendingIntent.getBroadcast(applicationContext, REQUEST_CODE, snoozeIntent, FLAGS)

    // TODO: Step 1.2 get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        // TODO: Step 1.8 use the new 'breakfast' notification channel
        applicationContext.getString(R.string.breakfast_notification_channel_id)
    )
        // TODO: Step 1.3 set title, text and icon to builder
        .setSmallIcon(R.drawable.account)
        .setContentTitle(applicationContext.getString(R.string.app_name))
        .setContentText(messageBody["msg"])
        // TODO: Step 1.13 set content intent
        .setContentIntent(pendingIntent)
        // TODO: Step 2.1 add style to builder

        // TODO: Step 2.5 set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    // TODO Step 1.4 call notify
    // Deliver the notification
    notify(NOTIFICATION_ID, builder.build())
}

// TODO: Step 1.14 Cancel all notifications
/**
 * Cancels all notifications.
 *
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}

private fun createChannel(context: Context,channelId: String, channelName: String) {
    // TODO: Step 1.6 START create a channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create channel to show notifications.
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            // TODO: Step 2.4 change importance
            NotificationManager.IMPORTANCE_HIGH
        )
            // TODO: Step 2.6 disable badges for this channel
            .apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = context.getString(R.string.breakfast_notification_channel_description)

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )

        notificationManager.createNotificationChannel(notificationChannel)

    }
    // TODO: Step 1.6 END create channel
}


//fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
//
//    createChannel(
//        applicationContext,
//        applicationContext.getString(R.string.breakfast_notification_channel_id),
//        applicationContext.getString(R.string.breakfast_notification_channel_name)
//    )
//
//    // TODO: Step 1.11 create intent
//    val contentIntent = Intent(applicationContext, MainActivity::class.java)
//
//    // TODO: Step 1.12 create PendingIntent
//    val contentPendingIntent = PendingIntent.getActivity(
//        applicationContext,
//        NOTIFICATION_ID,
//        contentIntent,
//        PendingIntent.FLAG_IMMUTABLE
//    )
//
//// TODO: Step 2.0 add style
//    val eggImage = BitmapFactory.decodeResource(
//        applicationContext.resources,
//        R.drawable.account
//    )
//    val bigPicStyle = NotificationCompat.BigPictureStyle()
//        .bigPicture(eggImage)
//        .bigLargeIcon(null)
//
//
//    // TODO: Step 2.2 add snooze action
////    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
////    val snoozePendingIntent: PendingIntent =
////        PendingIntent.getBroadcast(applicationContext, REQUEST_CODE, snoozeIntent, FLAGS)
//
//    // TODO: Step 1.2 get an instance of NotificationCompat.Builder
//    // Build the notification
//    val builder = NotificationCompat.Builder(
//        applicationContext,
//        // TODO: Step 1.8 use the new 'breakfast' notification channel
//        applicationContext.getString(R.string.breakfast_notification_channel_id)
//    )
//        // TODO: Step 1.3 set title, text and icon to builder
//        .setSmallIcon(R.drawable.account)
//        .setContentTitle(applicationContext.getString(R.string.app_name))
//        .setContentText(messageBody)
//        // TODO: Step 1.13 set content intent
//        .setContentIntent(contentPendingIntent)
//        // TODO: Step 2.1 add style to builder
//        .setStyle(bigPicStyle)
//        .setLargeIcon(eggImage)
//        // TODO: Step 2.3 add snooze action
//        .addAction(
//            R.drawable.like,
//            applicationContext.getString(R.string.add_your_review),
//            contentPendingIntent
//        )
//        // TODO: Step 2.5 set priority
//        .setPriority(NotificationCompat.PRIORITY_HIGH)
//        .setAutoCancel(true)
//
//    // TODO Step 1.4 call notify
//    // Deliver the notification
//    notify(NOTIFICATION_ID, builder.build())
//}