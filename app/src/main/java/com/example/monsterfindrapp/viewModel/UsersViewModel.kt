package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class UsersViewModel : ViewModel() {

    private val _suspendDate = MutableStateFlow<Date?>(null)
    val suspendDate: StateFlow<Date?> = _suspendDate.asStateFlow()

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
    fun callGetSuspendDate(user: User){
        viewModelScope.launch {
            _suspendDate.value = getSuspendDate(user)
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
                        Log.w("SuspendUser", "Error adding user in Suspended Users Collection ${e.message})", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Suspending User\"")
                    }
                _suspendDate.value = oneWeekFromNow
            }
            .addOnFailureListener{ e ->
                Log.w("SuspendUser", "Error Updating User in Users Collection ${e.message})", e)
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
                    "email" to user.email,
                    "isBanned" to true
                )
                // Place the new user in the banned users collection
                removedUsersRef.set(removedUser)
                    .addOnSuccessListener {
                        Log.i("BanUser", "User Added To Banned Users Collection Successfully")
                        LoadingStateManager.setIsSuccess(true)
                    }
                    .addOnFailureListener{ e->
                        Log.w("BanUser", "Error adding user in Banned Users Collection ${e.message})", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Banning User\"")
                    }
            }
            .addOnFailureListener{ e ->
                Log.w("BanUser", "Error Deleting User in Users Collection ${e.message})", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Banning User\"")
            }
    }

    private suspend fun getSuspendDate(user: User): Date?{
        if(!user.isSuspended){
            return null
        }
        val db = FirebaseFirestore.getInstance()
        val suspendedUserRef = db.collection("SuspendedUsers").document(user.uid)

        val suspendedUserDoc = suspendedUserRef.get().await()
        return if(suspendedUserDoc.exists()){
            val suspendDate = suspendedUserDoc.getTimestamp("suspendedDate")
            suspendDate?.toDate()
        }else {
            null
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
                        Log.w("UnSuspendUser", "Error Deleting User in Suspended Users Collection ${e.message})", e)
                        LoadingStateManager.setErrorMessage( e.message ?: "\"Error Un Suspending User\"")
                    }
            }
            .addOnFailureListener{ e->
                Log.w("UnSuspendUser", "Error Updating isSuspended User in Users Collection ${e.message})", e)
                LoadingStateManager.setErrorMessage( e.message ?: "\"Error Un Suspending User\"")
            }
    }

}
