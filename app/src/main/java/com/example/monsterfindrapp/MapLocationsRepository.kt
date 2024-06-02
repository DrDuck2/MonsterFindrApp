package com.example.monsterfindrapp

import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.StoreItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object MapLocationsRepository {

    private val _locations = MutableStateFlow<List<Locations>>(emptyList())
    val locations: StateFlow<List<Locations>> = _locations.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            getLocations().collect{ locations ->
                _locations.value = locations
            }
        }
    }

    private fun getLocations(): Flow<List<Locations>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("Locations").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val name = document.id
                val location = document.getGeoPoint("coordinates")!!
                val items = getItemsForLocation(db, name)
                Locations(
                    name = name,
                    location = location,
                    items = items
                )
            }
        }
    }

    private suspend fun getItemsForLocation(db: FirebaseFirestore, locationName: String): List<StoreItem>{
        val itemsSnapshot = db.collection("Locations").document(locationName).collection("Items").get().await()
        return itemsSnapshot.documents.map {document->
            val price = document.getDouble("price")!!
            val availability = document.getString("availability")!!
            val lastUpdated = document.getTimestamp("last_updated")!!.toDate()
            val monsterItemId = document.id
            val monsterItem = getMonsterItem(db, monsterItemId)
            StoreItem(
                price = price,
                availability = availability,
                lastUpdated = lastUpdated,
                monsterItem = monsterItem
            )
        }
    }

    private suspend fun getMonsterItem(db: FirebaseFirestore, monsterItemId: String): MonsterItem {
        val monsterItemSnapshot = db.collection("MonsterItems").document(monsterItemId).get().await()
        val id = monsterItemSnapshot.id
        val name = monsterItemSnapshot.getString("name")!!
        val description = monsterItemSnapshot.getString("desc")!!
        val imageUrl = monsterItemSnapshot.getString("image")!!
        return MonsterItem(
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl
        )
    }
}