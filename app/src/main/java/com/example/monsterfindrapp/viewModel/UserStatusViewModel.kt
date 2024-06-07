package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserStatusViewModel: ViewModel(){
    private val _isUserSuspended = MutableLiveData<Boolean?>()
    val isUserSuspended: LiveData<Boolean?> = _isUserSuspended

    private val _isUserBanned = MutableLiveData<Boolean>()
    val isUserBanned: LiveData<Boolean> = _isUserBanned



    fun checkUserStatus() {
        val db = FirebaseFirestore.getInstance()
        val email = AuthenticationManager.getCurrentUserEmail()

        viewModelScope.launch {
            while (true) {
                val isSuspended = withContext(Dispatchers.IO) {
                    try {
                        val querySnapshot = db.collection("Users").whereEqualTo("email", email).get().await()
                        if (querySnapshot.documents.isNotEmpty()) {
                            val userDoc = querySnapshot.documents[0]
                            userDoc.getBoolean("isSuspended")
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                val isBanned = withContext(Dispatchers.IO) {
                    try {
                        val querySnapshot = db.collection("BannedUsers").whereEqualTo("email", email).get().await()
                        querySnapshot.documents.isNotEmpty()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                _isUserSuspended.postValue(isSuspended)
                _isUserBanned.postValue(isBanned)

                Log.i("UserStatus Suspended: ", isSuspended.toString())
                Log.i("UsersStatus Banned: ", isBanned.toString())

                delay(5000) // Check every 5 seconds
            }
        }
    }

}