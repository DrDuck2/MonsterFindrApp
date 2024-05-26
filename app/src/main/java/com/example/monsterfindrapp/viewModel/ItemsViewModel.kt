package com.example.monsterfindrapp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ItemsViewModel {
    var showAddItemModal by mutableStateOf(false)

    fun showAddItemModal(){
        showAddItemModal = true
    }
    fun hideAddItemModal(){
        showAddItemModal = false
    }
}