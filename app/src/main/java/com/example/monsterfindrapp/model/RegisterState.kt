package com.example.monsterfindrapp.model

sealed class RegisterState{
    data object Idle : RegisterState()
    data object Loading : RegisterState()
    data object Success: RegisterState()
    data class Error(val message: String) : RegisterState()
}