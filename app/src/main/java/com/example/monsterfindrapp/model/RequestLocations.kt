package com.example.monsterfindrapp.model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class RequestLocations (
    var id: String,
    val coordinates: GeoPoint,
    val availability: String,
    val item: String,
    val price: Double,
    val imageProof: String,
    val createdAt: Date
)