package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.StoreItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class StoreItemsViewModel: ViewModel() {

    private val _selectedLocation = MutableStateFlow<Locations?>(null)
    val selectedLocation: StateFlow<Locations?> = _selectedLocation.asStateFlow()

    fun setSelectedLocation(location: Locations) {
        _selectedLocation.value = location
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
