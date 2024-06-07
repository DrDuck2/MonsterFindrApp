package com.example.monsterfindrapp.utility

import android.util.Log
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.LoginState
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.StoreItem
import com.example.monsterfindrapp.view.SmallLoadingIconOverlay
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

object MapLocationsRepository {

    private val _locations = MutableStateFlow<List<Locations>>(emptyList())
    val locations: StateFlow<List<Locations>> = _locations.asStateFlow()

    private val _locationItems = MutableStateFlow<Flow<List<StoreItem>>>(emptyFlow())
    val locationItems: StateFlow<Flow<List<StoreItem>>> = _locationItems.asStateFlow()


    init {
        CoroutineScope(Dispatchers.IO).launch {
            getLocations().collect{ locations ->
                _locations.value = locations
            }
        }
    }

    fun setLocationItems(items: Flow<List<StoreItem>>){
        _locationItems.value = items
    }

    fun getGeoPoints(): Flow<List<GeoPoint>> {
        return _locations.map{ list ->
            list.map {it.location}
        }
    }

    fun getFilteredLocationItems(query: String): Flow<List<StoreItem>> {
        return if (query.isEmpty()) {
            _locationItems.value
        } else {
            _locationItems.value.map { list ->
                list.filter { item ->
                    item.monsterItem.name.contains(query, ignoreCase = true)
                }
            }
        }
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

    fun getFilteredLocations(query: String): Flow<List<Locations>> {
        val list: Flow<List<Locations>> = if(query.isEmpty()){
            _locations.asStateFlow()
        }else{
            _locations.asStateFlow().map { locations ->
                locations.filter { location ->
                    location.items.map { storeItems ->
                        storeItems.any { storeItem ->
                            storeItem.monsterItem.name.contains(query, ignoreCase = true)
                        }
                    }.firstOrNull() ?: false
                }
            }
        }
        return list
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

    private suspend fun getItemsForLocation(db: FirebaseFirestore, locationName: String): Flow<List<StoreItem>> {
        return db.collection("Locations").document(locationName).collection("Items").snapshots()
            .map { snapshot ->
                snapshot.documents.map { document ->
                    val data = document.data
                    val price = data?.get("price") as? Double ?: 0.0
                    val availability = data?.get("availability") as? String ?: ""
                    val lastUpdated = data?.get("last_updated") as? Date ?: Date()
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