package com.example.monsterfindrapp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.model.LoginState
import com.example.monsterfindrapp.model.RegisterState
import com.example.monsterfindrapp.model.User
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

    private fun addUserToDatabase(email: String){
        val db = FirebaseFirestore.getInstance()

        val userId = AuthenticationManager.getCurrentUserId()
        val userDoc = db.collection("Users").document(userId!!).get()
        userDoc.addOnSuccessListener {  snapshot ->
            if(!snapshot.exists()){
                val user = hashMapOf(
                    "email" to email,
                    "isAdmin" to false,
                    "isSuspended" to false
                )
                db.collection("Users").document(userId).set(user)
            }
        }
        _loginState.value = LoginState.Success
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            var isSuspended: Boolean? = null
            var isBanned = false

            AuthenticationManager.checkUserSuspended(email, callback = { suspended ->
               isSuspended = suspended
                AuthenticationManager.checkUserBanned(email, callback = { banned ->
                    isBanned = banned
                    if(isBanned){
                        _loginState.value = LoginState.Error(message = "User Is Banned")
                    }else if(isSuspended == true){
                        _loginState.value = LoginState.Error(message = "User Is Suspended")
                    }else{
                        try {
                            val auth = Firebase.auth
                            viewModelScope.launch {
                                auth.signInWithEmailAndPassword(email, password).await()
                                addUserToDatabase(email)
                            }
                        } catch (e: Exception) {
                            _loginState.value = LoginState.Error(e.message ?: "Unknown Error")
                        }
                    }
                })
            })
        }
    }


}