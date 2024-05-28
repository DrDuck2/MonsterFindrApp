package com.example.monsterfindrapp.model

import com.google.firebase.firestore.GeoPoint

data class Locations (
    val name: String,
    val location: GeoPoint,
    val items: List<StoreItem>
)