package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.model.RequestLocations
import com.example.monsterfindrapp.model.RequestUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RequestsViewModel: ViewModel() {
    private val _usersRequests = MutableStateFlow<Flow<List<RequestLocations?>>>(emptyFlow())
    val usersRequests: StateFlow<Flow<List<RequestLocations?>>> = _usersRequests.asStateFlow()
    private val _selectedUser = MutableStateFlow<RequestUser?>(null)
    val user: StateFlow<RequestUser?> = _selectedUser.asStateFlow()
    private val _selectedRequest = MutableStateFlow<RequestLocations?>(null)
    val selectedRequest: StateFlow<RequestLocations?> = _selectedRequest.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    fun setShowDialog(value: Boolean){
        _showDialog.value = value
    }

    fun selectUser(selectedUser: RequestUser,user: Flow<List<RequestLocations>>) {
        _selectedUser.value = selectedUser
        _usersRequests.value = user
    }

    fun selectRequest(location: RequestLocations){
        _selectedRequest.value = location
    }

    fun removeRequest(user: RequestUser, request: RequestLocations){
        LoadingStateManager.setIsLoading(true)
        viewModelScope.launch {
            try{
                val db = Firebase.firestore
                val storage = FirebaseStorage.getInstance()

                val requestsCollection = db.collection("RequestEntries").document(user.id).collection("Requests")
                Log.d("RemoveRequest", "Removing Request with id: ${request.id}")
                val requestDoc = requestsCollection.document(request.id)
                requestDoc.delete().await()

                val proofImageUrl = request.imageProof
                if(proofImageUrl.isNotEmpty()){
                    val storageReference = storage.getReferenceFromUrl(proofImageUrl)
                    try{
                        storageReference.delete().await()
                        LoadingStateManager.setIsSuccess(true)
                    }catch (e: Exception){
                        Log.e("DeleteRequestError", "Error deleting proof image: ${e.message}", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Removing Request \"")
                    }
                }
            }catch (e: Exception){
                Log.e("DeleteRequestError", "Error deleting request: ${e.message}", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Removing Request \"")
            }

        }
    }

    fun addLocationAndApproveRequest(user: RequestUser, request: RequestLocations,newId: String){
        LoadingStateManager.setIsLoading(true)

        val db = Firebase.firestore

        viewModelScope.launch{
            try{
                val locationRef = db.collection("Locations").document(newId)

                val location = hashMapOf(
                    "coordinates" to request.coordinates
                )
                locationRef.set(location)
                    .addOnSuccessListener {
                        Log.d("AddLocationAndApproveRequest", "Location added successfully")
                    }
                    .addOnFailureListener{ e ->
                        Log.e("AddLocationAndApproveRequest","Error adding location: ${e.message}", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Entry \"")
                    }
                approveRequest(user, request,newId)
            }catch (e: Exception){
                Log.e("AddLocationAndApproveRequest", "Error adding Location: ${e.message}", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Entry \"")

            }
        }
    }



    fun approveRequest(user: RequestUser, request: RequestLocations,id: String) {
        val db = Firebase.firestore
        viewModelScope.launch {
            try {
                val locationsCollection =
                    db.collection("Locations").document(id).collection("Items")
                        .document(request.item)
                val entryData = hashMapOf(
                    "price" to request.price,
                    "availability" to request.availability,
                    "last_updated" to request.createdAt
                )
                locationsCollection.set(entryData)
                    .addOnSuccessListener {
                        Log.d("ApproveRequest", "Item updated/added successfully")
                        LoadingStateManager.setIsSuccess(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ApproveRequest", "Error updating/adding item: ${e.message}", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Entry \"")
                    }
            } catch (e: Exception) {
                Log.e("ApproveRequest", "Error updating item: ${e.message}", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Entry \"")
            }
            removeRequest(user, request)
        }
    }

    fun checkLocation(user: RequestUser, request: RequestLocations){
        viewModelScope.launch {
            checkWithExistingLocations(request.coordinates) {isSimilar ->
                if(isSimilar != null){
                    approveRequest(user, request, isSimilar.name)
                }else{
                    _showDialog.value = true
                }
            }
        }
    }

    private suspend fun checkWithExistingLocations(sentLocation: GeoPoint, callback:(Locations?) -> Unit){
        val db = FirebaseFirestore.getInstance()

        try {
            val locationsRef = db.collection("Locations").get().await()
            for(document in locationsRef.documents){
                val dbLocation = document.getGeoPoint("coordinates")
                if(dbLocation != null){
                    if(areCoordinatesSimilar(sentLocation, dbLocation))
                    {
                        val doc = Locations(
                            name = document.id,
                            location = dbLocation,
                            items = emptyFlow()
                        )
                        callback(doc)
                        return
                    }
                }
            }
            callback(null)
        }catch (e: Exception){
            e.printStackTrace()
            callback(null)
        }
    }

    private fun areCoordinatesSimilar(coordinates1: GeoPoint, coordinates2: GeoPoint): Boolean{
        val roundedLat1 = "%.3f".format(coordinates1.latitude).toDouble()
        val roundedLon1 = "%.3f".format(coordinates1.longitude).toDouble()
        val roundedLat2 = "%.3f".format(coordinates2.latitude).toDouble()
        val roundedLon2 = "%.3f".format(coordinates2.longitude).toDouble()

        return roundedLat1 == roundedLat2 && roundedLon1 == roundedLon2
    }
}