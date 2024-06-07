package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.MonsterRepository
import com.example.monsterfindrapp.utility.NotificationsRepository
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class HandleNotificationViewModel : ViewModel() {
    val userNotifications: StateFlow<List<Notification>> = NotificationsRepository.userNotifications

    private val monsterItems: StateFlow<List<MonsterItem>> = MonsterRepository.monsterItems

    private val _selectedDrink = MutableStateFlow<MonsterItem?>(null)
    val selectedDrink: StateFlow<MonsterItem?> = _selectedDrink

    fun filterMonsterItems(): List<MonsterItem> {
        val items = monsterItems.value
        val notifications = userNotifications.value.map { it.item }
        return items.filterNot { monsterItem -> notifications.any { it.id == monsterItem.id } }
    }

    fun selectDrink(drink: MonsterItem) {
        _selectedDrink.value = drink
    }


//    val userDocRef = db.collection("RequestEntries").document(currentUserId)
//    userDocRef.set(emptyMap<String, Any>()) // Create if it doesn't exist
//    .addOnSuccessListener {
//        val requestsCollectionRef = userDocRef.collection("Requests")
//
//        val storeLocationDocRef = requestsCollectionRef.document(storeLocation.name)
//
//        storeLocationDocRef.set(entryData)
//            .addOnSuccessListener {
//                Log.i("RequestEntry", "Entry submitted successfully")
//                LoadingStateManager.setIsSuccess(true)
//            }
//            .addOnFailureListener { e ->
//                Log.e("RequestEntry", "Error submitting entry: $e", e)
//                LoadingStateManager.setErrorMessage(e.message ?: "Error Submitting Entry")
//            }
//    }
//    .addOnFailureListener { e ->
//        Log.e("RequestEntry", "Error creating user document: $e", e)
//        LoadingStateManager.setErrorMessage(e.message ?: "Error Submitting Entry")
//    }

    fun submitNotification(){
        LoadingStateManager.setIsLoading(true)

        val db = FirebaseFirestore.getInstance()
        val userId = AuthenticationManager.getCurrentUserId()
        val userDocRef = db.collection("Notifications").document(userId!!)
        userDocRef.set(emptyMap<String, Any>())
            .addOnSuccessListener {
                val notificationsCollectionRef = userDocRef.collection("UserNotifications")
                val notificationData = hashMapOf(
                    "item" to selectedDrink.value?.id
                )

                notificationsCollectionRef.add(notificationData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("SubmitNotification", "Notification added with ID: ${documentReference.id}")
                        LoadingStateManager.setIsSuccess(true)
                        _selectedDrink.value = null
                    }
                    .addOnFailureListener{ e->
                        Log.e("SubmitNotification", "Error adding notification", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Notification To Database\"")
                    }
            }
            .addOnFailureListener{ e ->
                Log.e("SubmitNotification", "Error adding notification", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Notification To Database\"")
            }
    }

    fun removeNotification(notification: Notification) {
        LoadingStateManager.setIsLoading(true)

        val db = FirebaseFirestore.getInstance()
        val userId = AuthenticationManager.getCurrentUserId()
        val notificationRef =
            db.collection("Notifications").document(userId!!).collection("UserNotifications")

        notificationRef.whereEqualTo("item", notification.item.id)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    notificationRef.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("RemoveNotification", "Notification successfully deleted")
                            LoadingStateManager.setIsSuccess(true)
                        }
                        .addOnFailureListener { e ->
                            Log.w("RemoveNotification", "Error deleting notification", e)
                            LoadingStateManager.setErrorMessage(e.message ?: "\"Error Removing Notification From Database\"")
                        }
                }
            }.addOnFailureListener { e ->
                Log.w("RemoveNotification", "Error getting documents to delete", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Removing Notification From Database\"")
            }
    }
}