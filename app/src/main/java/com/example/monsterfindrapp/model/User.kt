package com.example.monsterfindrapp.model

import java.util.Date

data class User (
    val email: String,
    val uid: String,
    val isAdmin: Boolean,
    val isSuspended: Boolean,
    val isBanned: Boolean,
)