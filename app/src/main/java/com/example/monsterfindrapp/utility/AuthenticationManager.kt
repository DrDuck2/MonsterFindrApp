package com.example.monsterfindrapp.utility

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthenticationManager {
    private val auth = Firebase.auth
    private var currentUserId: String? = null

    fun isUserAuthenticated(): Boolean {
        val user = auth.currentUser
        if( user != null){
            currentUserId = user.uid
            return true
        }
        return false
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun checkUserIsAdmin(callback: (Boolean) -> Unit){
        CoroutineScope(Dispatchers.Main).launch {
            val isAdmin = userIsAdmin()
            callback(isAdmin)
        }
    }

    private suspend fun userIsAdmin(): Boolean{
        if(!isUserAuthenticated()){
            return false
        }
        val uid = getCurrentUserId()
        val db = FirebaseFirestore.getInstance()
        return try{
            val userDocument = uid?.let { db.collection("Users").document(it).get().await() }
            if(userDocument?.exists() == true){
                userDocument.getBoolean("isAdmin") ?: false
            }else{
                false
            }
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    fun logout(): Boolean{
        val auth = Firebase.auth
        auth.signOut()
        Log.i("Sign out", "User signed out")
        return !isUserAuthenticated()
    }

    fun navigateOnLoginFault(navController: NavController){
        if(!isUserAuthenticated()) {
            navController.navigate("LoginRegisterScreen")
        }
    }

    fun navigateOnAdminFault(navController: NavController){
        checkUserIsAdmin { isAdmin ->
            if (!isAdmin) {
                logout()
                navController.navigate("LoginRegisterScreen")
            }
        }
    }

    fun navigateOnUserRestricted(callback: (Boolean?) -> Unit){
        val userEmail = getCurrentUserEmail()
        checkUserSuspended(userEmail!!){ isSuspended->
            checkUserBanned(userEmail){ isBanned ->
                callback(isSuspended == true || isBanned)
            }
        }
    }


    fun checkUserSuspended(email: String, callback: (Boolean?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
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
            callback(isSuspended)
        }
    }

    fun checkUserBanned(email: String,callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
            val isBanned = withContext(Dispatchers.IO) {
                try {
                    val querySnapshot = db.collection("BannedUsers").whereEqualTo("email", email).get().await()
                    querySnapshot.documents.isNotEmpty()
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            callback(isBanned)
        }
    }
}