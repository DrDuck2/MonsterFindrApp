package com.example.monsterfindrapp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HandleNotificationViewModel : ViewModel() {
    var showNotificationModal by mutableStateOf(false)

    fun showNotificationModal(){
        showNotificationModal = true
    }
    fun hideNotificationModal(){
        showNotificationModal = false
    }

    fun selectDrink(){

    }
}