package com.example.monsterfindrapp.utility

import androidx.compose.runtime.collectAsState
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.StoreItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

object MapLocationsRepository {

    private val _locations = MutableStateFlow<List<Locations>>(emptyList())
    val locations: StateFlow<List<Locations>> = _locations.asStateFlow()

    private val _filteredLocations = MutableStateFlow<List<Locations>>(emptyList())
    val filteredLocations: StateFlow<List<Locations>> = _filteredLocations.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            getLocations().collect{ locations ->
                _locations.value = locations
            }
        }
    }

    fun getGeoPoints(): Flow<List<GeoPoint>> {
        return _locations.map{ list ->
            list.map {it.location}
        }
    }

    fun getStoreLocations(): Flow<List<Locations>>{
        return _locations.asStateFlow()
    }
    fun getFilteredStoreLocations(query: String): Flow<List<Locations>>{
        return if(query.isEmpty()){
            _locations.asStateFlow()
        }else{
            _locations.asStateFlow().map { locations ->
                locations.filter {location ->
                    location.name.contains(query, ignoreCase = true)
                }
            }
        }
    }


//    fun getFilteredLocations(query: String): Flow<List<Locations>> {
//        return if(query.isEmpty()){
//            _locations.asStateFlow()
//        }else{
//            _locations.asStateFlow().map { locationsList ->
//                locationsList.filter{ location ->
//                    location.items.map{ storeItemList ->
//                        storeItemList.any { storeItem ->
//                            storeItem.monsterItem.name.contains(query, ignoreCase = true)
//                        }
//                    }.firstOrNull {it} ?: false
//                }
//            }
//        }
//    }

    fun getFilteredLocations(query: String): Flow<List<Locations>> {
        return if(query.isEmpty()){
            _locations.asStateFlow()
        }else{
            _locations.asStateFlow().map { locations ->
                locations.filter {location ->
                    location.items.any {it.monsterItem.name.contains(query, ignoreCase = true)}
                }
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

//    private suspend fun getItemsForLocation(db: FirebaseFirestore, locationName: String): Flow<List<StoreItem>> {
//        return db.collection("Locations").document(locationName).collection("Items").snapshots()
//            .map { snapshot ->
//                snapshot.documents.map { document ->
//                    val data = document.data
//                    val price = data?.get("price") as? Double ?: 0.0
//                    val availability = data?.get("availability") as? String ?: ""
//                    val lastUpdated = data?.get("last_updated") as? Date ?: Date()
//                    val monsterItemId = document.id
//                    val monsterItem = getMonsterItem(db, monsterItemId)
//                    StoreItem(
//                        price = price,
//                        availability = availability,
//                        lastUpdated = lastUpdated,
//                        monsterItem = monsterItem
//                    )
//                }
//            }
//    }


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