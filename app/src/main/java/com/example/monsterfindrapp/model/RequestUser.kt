package com.example.monsterfindrapp.model

data class RequestUser (
    val id: String,
    val requestLocations: List<RequestLocations>,
    val userInfo: User
)