package com.example.monsterfindrapp.utility

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
    

}