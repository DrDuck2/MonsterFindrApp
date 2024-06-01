package com.example.monsterfindrapp.model

import java.util.Collections.copy

data class RequestUser (
    val id: String,
    var requestLocations: List<RequestLocations>,
    val userInfo: User
)