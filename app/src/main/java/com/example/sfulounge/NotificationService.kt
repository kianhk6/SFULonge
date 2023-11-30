package com.example.sfulounge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.MessageFormatter
import com.example.sfulounge.ui.messages.MessagesActivity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// run this as a foreground service
class NotificationService : Service() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var notificationId = 0
    private var listener: ListenerRegistration? = null
    private lateinit var userId: String
    private lateinit var notificationManager: NotificationManager

    private val _cache: HashMap<String, User> = HashMap()

    companion object {
        private const val NOTIFY_ID = -1
        private const val CHANNEL_ID = "sfu_lounge_messages"

        const val ACTION_LOGOUT = "com.example.sfulounge.ACTION_LOGOUT"
    }

    override fun onCreate() {
        super.onCreate()
        initializeNotifications()

        // this makes it into a foreground service
        initializeForeground()
    }

    private fun initializeNotifications() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // code from: https://developer.android.com/develop/ui/views/notifications/channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun initializeForeground() {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(getString(R.string.notification_foreground_title))
            setContentText(getString(R.string.notification_foreground_description))
            setSmallIcon(R.drawable.baseline_favorite_24)
        }
        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFY_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }
    }

    private fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Notification Service", "debug: onStartCommand called")
        if (intent?.action == ACTION_LOGOUT) {
            Log.d("Notification Service", "debug: action logout received. gracefully shutting down")
            removeListener()
            cancelAllNotifications()
            stopSelf()
        } else {
            removeListener()
            startListeningForMessages()
        }
        return START_STICKY
    }

    private fun sendNotification(chatRoom: ChatRoom) {
        val usersToFetch = chatRoom.members
            .filter { x -> x != userId && !_cache.contains(x) }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users = fetchUsers(usersToFetch)
                if (!isActive) {
                    Log.e("Notification Service", "error: coroutine cancelled")
                    stopSelf()
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    // wait for fetchUsers to stopSelf and then continue on the main thread
                    cacheUsers(users)
                    val title = formatTitle(chatRoom)
                    val text = formatText(chatRoom.mostRecentMessage!!)
                    sendNotificationImpl(chatRoom, title, text)
                }
            } catch (e: Exception) {
                Log.e("Notification Service", "error: ${e.message}")
                stopSelf()
            }
        }
    }

    private fun startListeningForMessages() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("Notification Service", "error: User is null")
            stopSelf()
            return
        }
        userId = user.uid
        listener = db.collection("chat_rooms")
            .whereArrayContains("members", user.uid)
            .orderBy("lastMessageSentTime", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e("Notification Service", "error: ${e.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val chatRoom = value.documents.firstOrNull()?.toObject(ChatRoom::class.java)
                        ?: return@addSnapshotListener
                    if (chatRoom.mostRecentMessage == null) {
                        return@addSnapshotListener
                    }
                    if (chatRoom.mostRecentMessage!!.senderId == user.uid) {
                        return@addSnapshotListener
                    }
                    val memberInfo = chatRoom.memberInfo[user.uid]
                        ?: return@addSnapshotListener
                    if (memberInfo.lastMessageSeenTime.isBefore(chatRoom.lastMessageSentTime)) {
                        sendNotification(chatRoom)
                    }
                }
            }
        Log.d("Notification Service", "debug: listening for messages...")
    }

    private fun sendNotificationImpl(chatRoom: ChatRoom, title: String, text: String) {
        val intent = Intent(this, MessagesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MessagesActivity.INTENT_CHATROOM_ID, chatRoom.roomId)

            val members = ArrayList<String>()
            members.addAll(chatRoom.members)

            putExtra(MessagesActivity.INTENT_MEMBER_IDS, members)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(text)
            setSmallIcon(R.drawable.baseline_favorite_24)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }
        val notification = notificationBuilder.build()

        notificationManager.notify(notificationId++, notification)
    }

    private fun formatTitle(chatRoom: ChatRoom): String {
        val user = auth.currentUser ?: throw IllegalStateException("User is null")
        val members = chatRoom.members.mapNotNull { x -> _cache[x] }
        return MessageFormatter.formatNames(members, user.uid)
    }

    private fun formatText(message: Message): String {
        val sender = _cache[message.senderId]
        return MessageFormatter.formatMessageSummary(message, sender)
    }

    private suspend fun fetchUsers(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) {
            return emptyList()
        }
        return db.collection("users")
            .whereIn("userId", userIds)
            .get()
            .await()
            .documents.mapNotNull { x -> x.toObject(User::class.java) }
    }

    private fun cacheUsers(users: List<User>) {
        for (user in users) {
            _cache[user.userId] = user
        }
    }

    private fun removeListener() {
        listener?.remove()
        listener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        removeListener()
        Log.d("Notification Service", "debug: onDestroy called")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun Timestamp.isBefore(timestamp: Timestamp?): Boolean {
        if (timestamp == null) {
            return false
        }
        return compareTo(timestamp) < 0
    }
}