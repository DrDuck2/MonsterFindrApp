package com.example.monsterfindrapp.model

import java.util.Collections.copy

data class RequestUser (
    val id: String,
    val requestLocations: List<RequestLocations>,
    val userInfo: User
)