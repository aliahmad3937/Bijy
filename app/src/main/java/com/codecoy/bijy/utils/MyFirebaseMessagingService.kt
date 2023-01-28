package com.codecoy.bijy.utils


import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.codecoy.bijy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("TAG7", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("TAG7", "Message data payload: ${remoteMessage.data}")

//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob()
//            } else {
//                // Handle message within 10 seconds
//                handleNow()
//            }

            val sp: SharedPreferences = getSharedPreferences(getString(R.string.my_preferences), Context.MODE_PRIVATE)
            val user:String = sp.getString(Constants.USER_TYPE,"").toString()
            if(user.isNotEmpty() && user == UserType.User.toString()) {
                sendNotification(remoteMessage.data)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("TAG7", "Message Notification Body: ${it.body}")
              val sp: SharedPreferences = getSharedPreferences(getString(R.string.my_preferences), Context.MODE_PRIVATE)
            val user:String = sp.getString(Constants.USER_TYPE,"").toString()
            if(user.isNotEmpty() && user == UserType.User.toString()) {
                sendNotification(it.body as String)
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun sendNotification(messageBody: MutableMap<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.sendNotification(messageBody, applicationContext)
    }


    override fun onNewToken(token: String) {
        Log.v("TAG7", "token :$token")
        SavedPreference.setToken(applicationContext,token)
        super.onNewToken(token)
    }


    private fun sendNotification(messageBody: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.sendNotification(messageBody, applicationContext)
    }

}