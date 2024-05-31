package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.Notification
import com.example.monsterfindrapp.model.StoreItem
import com.example.monsterfindrapp.model.UserNotifications
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HandleNotificationViewModel : ViewModel() {
    var showNotificationModal by mutableStateOf(false)

    fun showNotificationModal(){
        showNotificationModal = true
    }
    fun hideNotificationModal(){
        showNotificationModal = false
    }


    private val _userNotifications = MutableStateFlow<UserNotifications?>(null)
    val userNotifications: StateFlow<UserNotifications?> = _userNotifications

    private val _monsterItems = MutableStateFlow<List<MonsterItem>>(emptyList())
    val monsterItems: StateFlow<List<MonsterItem>> = _monsterItems.asStateFlow()

    private val _selectedDrink = MutableStateFlow<MonsterItem?>(null)
    val selectedDrink: StateFlow<MonsterItem?> = _selectedDrink

    fun selectDrink(drink: MonsterItem) {
        _selectedDrink.value = drink
    }

    init {
        viewModelScope.launch {
            // Fetch notifications
            getUserNotifications().collect { notifications ->
                _userNotifications.value = notifications
            }

            getMonsterItems().collect {items ->
                _monsterItems.value = items
            }
        }
    }

    private fun getMonsterItems(): Flow<List<MonsterItem>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("MonsterItems").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val data = document.data
                MonsterItem(
                    document.id,
                    data?.get("name") as? String ?: "",
                    data?.get("desc") as? String ?: "",
                    data?.get("image") as? String ?: ""
                )
            }
        }
    }

    private fun getUserNotifications(): Flow<UserNotifications?> {
        val db = FirebaseFirestore.getInstance()
        val userId = AuthenticationManager.getCurrentUserId()

        return flow{
            val notifications = getNotificationsForUser(db, userId!!)
            emit(UserNotifications(id = userId, notifications = notifications))
        }
    }

    private suspend fun getNotificationsForUser(db: FirebaseFirestore, userId: String): List<Notification> {
        val notificationsSnapshot = db.collection("Notifications")
            .document(userId)
            .collection("UserNotifications")
            .get()
            .await()
        return notificationsSnapshot.documents.map {document ->
            val item = document.getString("item")!!
            val monsterItem = getUserNotificationItems(db, item)
            Notification(item = monsterItem)
        }
    }

    private suspend fun getUserNotificationItems(db: FirebaseFirestore, item:String): MonsterItem {
        val itemSnapshot = db.collection("MonsterItems")
            .document(item)
            .get()
            .await()

        return MonsterItem(
            id = itemSnapshot.id,
            name = itemSnapshot.getString("name") ?: "",
            description = itemSnapshot.getString("desc") ?: "",
            imageUrl = itemSnapshot.getString("image") ?: ""
        )

    }


    fun submitNotification(){
        val db = FirebaseFirestore.getInstance()
        val userId = AuthenticationManager.getCurrentUserId()
        val notificationRef = db.collection("Notifications").document(userId!!).collection("UserNotifications")

        val notificationData = hashMapOf(
            "item" to selectedDrink.value?.id
        )

        notificationRef.add(notificationData)
            .addOnSuccessListener { documentReference ->
                Log.d("SubmitNotification", "Notification added with ID: ${documentReference.id}")
            }
            .addOnFailureListener{ e->
                Log.w("SubmitNotification", "Error adding notification", e)
            }

        updateItemsLocally()
    }

    fun removeNotification(notification: Notification){
        val db = FirebaseFirestore.getInstance()
        val userId = AuthenticationManager.getCurrentUserId()
        val notificationRef = db.collection("Notifications").document(userId!!).collection("UserNotifications")

        notificationRef.whereEqualTo("item", notification.item.id)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    notificationRef.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("RemoveNotification", "Notification successfully deleted")
                            updateItemsLocally(notification)
                        }
                        .addOnFailureListener { e->
                            Log.w("RemoveNotification", "Error deleting notification", e)
                        }
                }
            }.addOnFailureListener{ e->
                Log.w("RemoveNotification", "Error getting documents to delete", e)
            }
    }

    private fun updateItemsLocally(){
        val updatedUserNotifications = _userNotifications.value ?: return
        val drink =_selectedDrink.value ?: return

        val updatedNotifications = updatedUserNotifications.notifications.toMutableList()
        updatedNotifications.add(Notification(drink))

        _userNotifications.value = updatedUserNotifications.copy(notifications = updatedNotifications)

        _selectedDrink.value = null
    }

    private fun updateItemsLocally(notification: Notification){
        val updatedUserNotifications = _userNotifications.value ?: return

        val updatedNotifications = updatedUserNotifications.notifications.toMutableList()
        updatedNotifications.remove(notification)

        _userNotifications.value = updatedUserNotifications.copy(notifications = updatedNotifications)
    }

}