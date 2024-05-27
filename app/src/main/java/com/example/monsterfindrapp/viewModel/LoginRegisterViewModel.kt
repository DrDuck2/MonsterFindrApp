package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.LoginState
import com.example.monsterfindrapp.model.RegisterState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginRegisterViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    var showRegisterModal by mutableStateOf(false)

    fun showRegisterModal(){
        showRegisterModal = true
    }
    fun hideRegisterModal(){
        showRegisterModal = false
    }

    fun register(email: String, password: String, repeatPassword: String){
        if(password != repeatPassword){
            return
        }
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val auth = Firebase.auth
                auth.createUserWithEmailAndPassword(email, password).await()
                _registerState.value = RegisterState.Success

                login(email,password)
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val auth = Firebase.auth
                auth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun checkUserIsAdmin(callback: (Boolean) -> Unit){
        CoroutineScope(Dispatchers.Main).launch {
            val isAdmin = userIsAdmin()
            callback(isAdmin)
        }
    }

    private suspend fun userIsAdmin(): Boolean{
        if(!AuthenticationManager.isUserAuthenticated()){
            return false
        }
        val uid = AuthenticationManager.getCurrentUserId()
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
}