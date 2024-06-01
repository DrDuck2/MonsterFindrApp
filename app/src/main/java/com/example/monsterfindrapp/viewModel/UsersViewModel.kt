package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class UsersViewModel : ViewModel() {
    private val _user = MutableStateFlow<List<User>>(emptyList())
    //val user: StateFlow<List<User>> = _user.asStateFlow()

    private val _suspendDate = MutableLiveData<Date?>()
    val suspendDate: LiveData<Date?> = _suspendDate

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun resetLoading(){
        _isLoading.value = false
        _isSuccess.value = false
        _errorMessage.value = null
    }

    fun getFilteredUsers(query: String, selectedFilter: String): Flow<List<User>> {
        return if (query.isEmpty() && (selectedFilter.isEmpty() || selectedFilter == "All")) {
            _user.asStateFlow()
        } else {
            _user.asStateFlow().map { users ->
                var filteredUsers = users
                if (selectedFilter.isNotEmpty()) {
                    filteredUsers = filteredUsers.filter { user ->
                        when (selectedFilter) {
                            "Suspended" -> user.isSuspended
                            "Admin" -> user.isAdmin
                            "Regular" -> !(user.isAdmin)
                            else -> true
                        }
                    }
                }
                if (query.isNotEmpty()) {
                    filteredUsers = filteredUsers.filter { user ->
                        user.email.contains(query, ignoreCase = true) ?: false ||
                                user.uid.contains(query, ignoreCase = true)
                    }
                }
                filteredUsers
            }
        }
    }

    init {
        viewModelScope.launch() {
            getUsers().collect { items ->
                _user.value = items
                sortUsers()
            }
        }
    }

    private fun getUsers(): Flow<List<User>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("Users").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val data = document.data
                User(
                    data?.get("email") as? String ?: "",
                    document.id,
                    data?.get("isAdmin") as? Boolean ?: false,
                    data?.get("isSuspended") as? Boolean ?: false,
                    data?.get("isBanned") as? Boolean ?: false,
                )
            }
        }
    }

    fun getUserColor(user: User): Color{
        return when {
            user.isAdmin && user.uid == AuthenticationManager.getCurrentUserId() -> Color(0xFF90EE90)
            user.isAdmin -> Color(0xFFADD8E6)
            user.isSuspended -> Color(0xFFFFFF00)
            else -> Color.White
        }
    }

    private fun sortUsers() {
        val currentUserId = AuthenticationManager.getCurrentUserId()
        _user.value = _user.value.sortedWith(
            compareByDescending<User> { it.uid == currentUserId }
                .thenByDescending { it.isAdmin }
                .thenByDescending { it.isSuspended }
                .thenBy { it.email }
        )
    }

    fun callSuspendUser(user:User){
        _isLoading.value = true
        viewModelScope.launch {
            suspendUser(user)
        }
    }

    fun callBanUser(user:User){
        _isLoading.value = true
        viewModelScope.launch {
            banUser(user)
        }
    }

    fun callUnSuspendUser(user: User){
        _isLoading.value = true
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
                        _isSuccess.value = true
                    }
                    .addOnFailureListener{ e ->
                        Log.w("SuspendUser", "Error adding user in Suspended Users Collection ${e.message})", e)
                        _errorMessage.value = e.message ?: "\"Error Suspending User\""
                    }
                _suspendDate.value = oneWeekFromNow
            }
            .addOnFailureListener{ e ->
                Log.w("SuspendUser", "Error Updating User in Users Collection ${e.message})", e)
                _errorMessage.value = e.message ?: "\"Error Suspending User\""
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
                        _isSuccess.value = true
                    }
                    .addOnFailureListener{ e->
                        Log.w("BanUser", "Error adding user in Banned Users Collection ${e.message})", e)
                        _errorMessage.value = e.message ?: "\"Error Banning User\""
                    }
            }
            .addOnFailureListener{ e ->
                Log.w("BanUser", "Error Deleting User in Users Collection ${e.message})", e)
                _errorMessage.value = e.message ?: "\"Error Banning User\""
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
                        _isSuccess.value = true
                    }
                    .addOnFailureListener{ e->
                        Log.w("UnSuspendUser", "Error Deleting User in Suspended Users Collection ${e.message})", e)
                        _errorMessage.value = e.message ?: "\"Error Un Suspending User\""
                    }
            }
            .addOnFailureListener{ e->
                Log.w("UnSuspendUser", "Error Updating isSuspended User in Users Collection ${e.message})", e)
                _errorMessage.value = e.message ?: "\"Error Un Suspending User\""
            }
    }

}
