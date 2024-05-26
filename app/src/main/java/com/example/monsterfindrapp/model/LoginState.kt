package com.example.monsterfindrapp.model

sealed class LoginState{
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success: LoginState()
    data class Error(val message: String) : LoginState()
}
