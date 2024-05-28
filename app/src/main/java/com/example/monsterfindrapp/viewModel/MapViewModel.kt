package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.StoreItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel : ViewModel() {

    private val _locations = MutableStateFlow<List<Locations>>(emptyList())
    val locations: StateFlow<List<Locations>> = _locations.asStateFlow()

    val selectedLocation = mutableStateOf<Locations?>(null)
    val isMapExpanded = mutableStateOf(true)

    init {
        viewModelScope.launch() {
            getLocations().collect { locations ->
                _locations.value = locations
            }
        }
    }

    fun logout(): Boolean{
        val auth = Firebase.auth
        auth.signOut()
        Log.i("Sign out", "User signed out")
        return !AuthenticationManager.isUserAuthenticated()
    }

    private fun getLocations(): Flow<List<Locations>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("Locations").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val name = document.id
                val location = document.getGeoPoint("coordinates")!!
                Log.i("Name", name)
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
            Log.i("Monster Id", monsterItemId)
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
        val name = monsterItemSnapshot.getString("name")!!
        val description = monsterItemSnapshot.getString("desc")!!
        val imageUrl = monsterItemSnapshot.getString("image")!!
        return MonsterItem(
            name = name,
            description = description,
            imageUrl = imageUrl
        )
    }

}