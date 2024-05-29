package com.example.monsterfindrapp.model

import com.google.firebase.firestore.GeoPoint

data class RequestLocations (
    val id: String,
    val coordinates: GeoPoint,
    val availability: String,
    val item: String,
    val price: Double,
    val imageProof: String
)