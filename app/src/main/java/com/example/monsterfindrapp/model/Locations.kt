package com.example.monsterfindrapp.model

import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.Flow

data class Locations (
    val name: String,
    val location: GeoPoint,
//    val items: Flow<List<StoreItem>>
    val items: List<StoreItem>
)