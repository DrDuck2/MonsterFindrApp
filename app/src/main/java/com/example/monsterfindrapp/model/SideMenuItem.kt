package com.example.monsterfindrapp.model

import androidx.navigation.NavController

data class SideMenuItem(
    val text: String,
    val action: (NavController) -> Unit
)