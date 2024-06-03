package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class UsersViewModel : ViewModel() {

    private val suspendDateMap = mutableMapOf<String, MutableStateFlow<Date?>>()
    //Call DB actions in a coroutine scope to avoid blocking the UI thread

    fun callSuspendUser(user:User){
        LoadingStateManager.setIsLoading(true)
        viewModelScope.launch {
            suspendUser(user)
        }
    }
    fun callBanUser(user:User){
        LoadingStateManager.setIsLoading(true)
        viewModelScope.launch {
            banUser(user)
        }
    }
    fun callUnSuspendUser(user: User){
        LoadingStateManager.setIsLoading(true)
        viewModelScope.launch {
            unSuspendUser(user)
        }
    }

    fun getSuspendDate(user: User): StateFlow<Date?> {
        val userId = user.uid
        if (!suspendDateMap.containsKey(userId)) {
            suspendDateMap[userId] = MutableStateFlow(null)
            fetchSuspendDate(user)
        }
        return suspendDateMap[userId]!!.asStateFlow()
    }

    private fun fetchSuspendDate(user: User) {
        viewModelScope.launch {
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            db.collection("SuspendedUsers").document(userId).snapshots().map { snapshot ->
                val date = snapshot.getTimestamp("suspendedDate")?.toDate()
                date
            }.collect { date ->
                suspendDateMap[userId]?.value = date
                Log.i("CollectSuspendDate", "Date collected for $userId: $date")
            }
        }
    }

    private fun suspendUser(user: User){
        val db = FirebaseFirestore.getInstance()
        //Reference to the Users Collection and Specific User from the card
        val userRef = db.collection("Users").document(user.uid)
        // Reference to the new Banned Users Collection
        val suspendedUsersRef = db.collection("SuspendedUsers").document(user.uid)

        //Set the user to suspended in Users collection
        userRef.update("isSuspended", true)
            .addOnSuccessListener {

                Log.i("SuspendUser", "User Updated")
                // Suspended time is set to 7 days from now
                val cal = Calendar.getInstance()
                cal.add(Calendar.DATE, 7) // Add 7 days
                val oneWeekFromNow = cal.time

                val suspendedUser = hashMapOf(
                    "suspendedDate" to oneWeekFromNow
                )
                // Place the new user in the suspended users collection
                suspendedUsersRef.set(suspendedUser)
                    .addOnSuccessListener {
                        Log.i("SuspendUser", "User Added")
                        LoadingStateManager.setIsSuccess(true)
                    }
                    .addOnFailureListener{ e ->
                        Log.e("SuspendUser", "Error adding user in Suspended Users Collection ${e.message})", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Suspending User\"")
                    }
            }
            .addOnFailureListener{ e ->
                Log.e("SuspendUser", "Error Updating User in Users Collection ${e.message})", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Suspending User\"")
            }
    }
    private fun banUser(user: User){
        val db = FirebaseFirestore.getInstance()
        //Reference to the Users Collection and Specific User from the card
        val userRef = db.collection("Users").document(user.uid)
        // Reference to the new Banned Users Collection
        val removedUsersRef = db.collection("BannedUsers").document(user.uid)

        //Delete the user from the Users collection
        userRef.delete()
            .addOnSuccessListener {
                Log.i("BanUser", "User Ref Deleted Successfully")
                val removedUser = hashMapOf(
                    "email" to user.email
                )
                // Place the new user in the banned users collection
                removedUsersRef.set(removedUser)
                    .addOnSuccessListener {
                        Log.i("BanUser", "User Added To Banned Users Collection Successfully")

                        val notificationsRef = db.collection("Notifications").document(user.uid)
                        notificationsRef.delete()
                            .addOnSuccessListener {
                                val requestEntriesRef = db.collection("RequestEntries").document(user.uid)
                                requestEntriesRef.delete()
                                    .addOnSuccessListener {
                                        Log.i("BanUser", "User Successfully Removed From Database")
                                        LoadingStateManager.setIsSuccess(true)
                                    }
                                    .addOnFailureListener{ e->
                                        Log.e("BanUser", "Error Removing User From RequestEntries ${e.message})", e)
                                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Banning User\"")
                                    }
                            }.addOnFailureListener{e->
                                Log.e("BanUser", "Error Removing User From Notifications ${e.message})", e)
                                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Banning User\"")
                            }
                        LoadingStateManager.setIsSuccess(true)
                    }
                    .addOnFailureListener{ e->
                        Log.e("BanUser", "Error adding user in Banned Users Collection ${e.message})", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Banning User\"")
                    }
            }
            .addOnFailureListener{ e ->
                Log.e("BanUser", "Error Deleting User in Users Collection ${e.message})", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Banning User\"")
            }
    }

    private fun unSuspendUser(user: User){
        val db = FirebaseFirestore.getInstance()

        val userRef = db.collection("Users").document(user.uid)
        userRef.update("isSuspended", false)
            .addOnSuccessListener {
                Log.i("UnSuspendUser", "Users Field Updated Successfully")
                val suspendedUserRef = db.collection("SuspendedUsers").document(user.uid)
                suspendedUserRef.delete()
                    .addOnSuccessListener {
                        Log.i("UnSuspendUser", "User Deleted From Suspended Users Collection Successfully")
                        LoadingStateManager.setIsSuccess(true)
                    }
                    .addOnFailureListener{ e->
                        Log.e("UnSuspendUser", "Error Deleting User in Suspended Users Collection ${e.message})", e)
                        LoadingStateManager.setErrorMessage( e.message ?: "\"Error Un Suspending User\"")
                    }
            }
            .addOnFailureListener{ e->
                Log.e("UnSuspendUser", "Error Updating isSuspended User in Users Collection ${e.message})", e)
                LoadingStateManager.setErrorMessage( e.message ?: "\"Error Un Suspending User\"")
            }
    }

}
