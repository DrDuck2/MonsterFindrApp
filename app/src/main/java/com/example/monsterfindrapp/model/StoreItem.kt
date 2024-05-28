package com.example.monsterfindrapp.model

import java.util.Date


data class StoreItem(
    val price: Double,
    val availability: String,
    val lastUpdated: Date,
    val monsterItem: MonsterItem
    )