package it.polito.mad.project.services

import it.polito.mad.project.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.project.MainActivity
import it.polito.mad.project.repositories.UserRepository
import kotlin.random.Random


class MessageService : FirebaseMessagingService() {

    private val adminChannelId = "admin_channel"
    private val userRepository = UserRepository()
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt(3000)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val buyerId = remoteMessage.data["BuyerId"]?:""
        var message = remoteMessage.data["message"]
        var idDestination = R.id.showItemFragment
        if (buyerId.isNotBlank() && buyerId == userRepository.getFirebaseUser()?.uid) {
            message = "Congratulations on your new purchase! You can rate it."
            idDestination = R.id.itemReviewFragment
            FirebaseMessaging.getInstance()
                .unsubscribeFromTopic("/topics/${remoteMessage.data["ItemId"]}")
        }

        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(idDestination)
            .setArguments(bundleOf("ItemId" to remoteMessage.data["ItemId"], "IsMyItem" to (remoteMessage.data["IsMyItem"] == "true")))
            .createPendingIntent()

        val largeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_notifications_black_24dp
        )

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, adminChannelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setLargeIcon(largeIcon)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.colorPrimaryDark)
        }
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(adminChannelId, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }
}