package com.example.monsterfindrapp.utility

import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object NotificationsRepository {

    private val _userNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val userNotifications: StateFlow<List<Notification>> = _userNotifications.asStateFlow()


    init {
        CoroutineScope(Dispatchers.IO).launch {
            getUserNotifications().collect{ notifications ->
                _userNotifications.value = notifications
            }
        }
    }

    private fun getUserNotifications(): Flow<List<Notification>> {
        val db = FirebaseFirestore.getInstance()
        val userId = AuthenticationManager.getCurrentUserId()

        return db.collection("Notifications").document(userId!!).collection("UserNotifications").snapshots().map{ snapshot ->
            snapshot.documents.map { document ->
                val data = document.data
                Notification(
                    item = getUserNotificationItems(db, data?.get("item") as? String ?: "")
                )
            }
        }
    }

    private suspend fun getUserNotificationItems(db: FirebaseFirestore, item:String): MonsterItem {
        return try {
            val snapshot = db.collection("MonsterItems").document(item).get().await()
            val data = snapshot.data
            MonsterItem(
                item,
                data?.get("name") as? String ?: "",
                data?.get("desc") as? String ?: "",
                data?.get("image") as? String ?: ""
            )
        } catch (e: Exception) {
            // Handle exceptions
            MonsterItem("", "", "", "")
        }
    }
}