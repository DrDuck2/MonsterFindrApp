package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.StoreItem
import com.example.monsterfindrapp.utility.MapLocationsRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class StoreItemsViewModel: ViewModel() {


    private val _selectedDrink = MutableStateFlow<MonsterItem?>(null)
    val selectedDrink: StateFlow<MonsterItem?> = _selectedDrink

    private val _selectedLocation = MutableStateFlow<Locations?>(null)
    val selectedLocation: StateFlow<Locations?> = _selectedLocation.asStateFlow()

    fun selectLocation(selectedLocation: Locations) {
        MapLocationsRepository.setLocationItems(selectedLocation.items)
        _selectedLocation.value = selectedLocation
    }

    fun selectDrink(drink: MonsterItem) {
        _selectedDrink.value = drink
    }

    fun addStore(store: String, latitude: Double, longitude: Double){
        LoadingStateManager.setIsLoading(true)
        val db = FirebaseFirestore.getInstance()

        val geoPoint = GeoPoint(latitude,longitude)

        val storeData = hashMapOf(
            "coordinates" to geoPoint
        )

        db.collection("Locations").document(store).set(storeData)
            .addOnSuccessListener {
                LoadingStateManager.setIsSuccess(true)
                Log.i("AddStore", "Store Successfully Added")
            }
            .addOnFailureListener{ e ->
                Log.e("AddStore", "Error Adding Store To Database: ${e.message}", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Store To Database\"")
            }
    }

    fun removeStore(store: Locations) {
        LoadingStateManager.setIsLoading(true)
        val db = FirebaseFirestore.getInstance()

        db.collection("Locations").document(store.name).delete()
            .addOnSuccessListener {
                LoadingStateManager.setIsSuccess(true)
                Log.i("RemoveStore", "Store Successfully Removed")

            }
            .addOnFailureListener { e ->
                Log.e("RemoveStore", "Error Removing the Store From Database: ${e.message}", e)
                LoadingStateManager.setErrorMessage(
                    e.message ?: "\"Error Removing the Store From Database\""
                )
            }
    }

    fun addStoreItem(item: MonsterItem, price: Double, availability: String){
        LoadingStateManager.setIsLoading(true)
        val db = FirebaseFirestore.getInstance()

        val storeItem = hashMapOf(
            "availability" to availability,
            "price" to price,
            "last_updated" to Timestamp.now()
        )

        db.collection("Locations").document(selectedLocation.value!!.name).collection("Items").document(item.id).set(storeItem)
            .addOnSuccessListener {
                LoadingStateManager.setIsSuccess(true)
                Log.i("AddStoreItem", "Item Successfully Added")
            }
            .addOnFailureListener{ e->
                Log.e("AddStoreItem", "Error Adding Item to Database: ${e.message}", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Item\""
                )
            }


    }

    fun removeStoreItem(item: StoreItem) {
        LoadingStateManager.setIsLoading(true)
        val db = FirebaseFirestore.getInstance()

        db.collection("Locations").document(selectedLocation.value!!.name).collection("Items")
            .document(item.monsterItem.id).delete()
            .addOnSuccessListener {
                LoadingStateManager.setIsSuccess(true)
                Log.i("RemoveStoreItem", "Item Successfully Removed")
            }
            .addOnFailureListener { e ->
                Log.e(
                    "RemoveStoreItem",
                    "Error Removing Item From the Store in Database: ${e.message}",
                    e
                )
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Removing Item\"")
            }
    }

}
