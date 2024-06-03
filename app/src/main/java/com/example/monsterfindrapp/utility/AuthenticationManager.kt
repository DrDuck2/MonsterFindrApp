package com.example.monsterfindrapp.utility

import android.util.Log
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    fun checkUserIsAdmin(callback: (Boolean) -> Unit){
        CoroutineScope(Dispatchers.Main).launch {
            val isAdmin = userIsAdmin()
            callback(isAdmin)
        }
    }

    private suspend fun userIsAdmin(): Boolean{
        if(isUserAuthenticated()){
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
        return !AuthenticationManager.isUserAuthenticated()
    }

    fun navigateOnLoginFault(navController: NavController){
        if(!isUserAuthenticated()) {
            navController.navigate("LoginRegisterScreen")
        }
    }

    fun navigateOnAdminFault(navController: NavController){
        checkUserIsAdmin { isAdmin ->
            if (isAdmin) {
                logout()
                navController.navigate("LoginRegisterScreen")
            }
        }
    }

}