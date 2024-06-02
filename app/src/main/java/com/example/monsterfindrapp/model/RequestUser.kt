package com.example.monsterfindrapp.model

import kotlinx.coroutines.flow.Flow

data class RequestUser (
    val id: String,
    var requestLocations: Flow<List<RequestLocations>>,
    val userInfo: User
)