package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.AuthenticationManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AdminDashboardViewModel : ViewModel() {

    fun logout(): Boolean{
        val auth = Firebase.auth
        auth.signOut()
        Log.i("Sign out", "User signed out")
        return !AuthenticationManager.isUserAuthenticated()
    }
}