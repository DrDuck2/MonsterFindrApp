package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.RequestLocations
import com.example.monsterfindrapp.model.RequestUser
import com.example.monsterfindrapp.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class RequestsViewModel: ViewModel() {
    private val _requests = MutableStateFlow<List<RequestUser>>(emptyList())
    val requests: StateFlow<List<RequestUser>> = _requests

    private val _selectedUser = MutableStateFlow<RequestUser?>(null)
    val selectedUser: StateFlow<RequestUser?> = _selectedUser

    private val _locations = MutableStateFlow<List<GeoPoint>>(emptyList())
    val locations: StateFlow<List<GeoPoint>> = _locations

    private val _selectedRequest = MutableStateFlow<RequestLocations?>(null)
    val selectedRequest: StateFlow<RequestLocations?> = _selectedRequest

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun resetLoading(){
        _isLoading.value = false
        _isSuccess.value = false
        _errorMessage.value = null
    }

    fun selectUser(user: RequestUser) {
        _selectedUser.value = user
        sortRequestLocations()
    }

    fun selectRequest(location: RequestLocations){
        _selectedRequest.value = location
    }

    init {
        viewModelScope.launch {
            getRequests().collect { items ->
                _requests.value = items
                sortRequests()
            }
            getLocations().collect { locations ->
                _locations.value = locations
            }
        }
    }

    private fun getRequests(): Flow<List<RequestUser>> = flow {
        val db = FirebaseFirestore.getInstance()
        val requestEntriesSnapshot = db.collection("RequestEntries").get().await()
        val allRequestUsers = mutableListOf<RequestUser>()

        for (userDocument in requestEntriesSnapshot.documents) {
            val userId = userDocument.id
            val userRequestSnapshot = db.collection("RequestEntries")
                .document(userId)
                .collection("Requests")
                .get()
                .await()

            val requestLocations = userRequestSnapshot.documents.map { requestDocument ->
                val data = requestDocument.data
                RequestLocations(
                    id = requestDocument.id,
                    coordinates = data?.get("coordinates") as? GeoPoint ?: GeoPoint(0.0, 0.0),
                    availability = data?.get("availability") as? String ?: "",
                    item = data?.get("item") as? String ?: "",
                    price = (data?.get("price") as? Number)?.toDouble() ?: 0.0,
                    imageProof = data?.get("proof_image") as? String ?: "",
                    created_at = (data?.get("created_at") as? Timestamp)?.toDate() ?: Date()
                )
            }
            val userSnapshot = db.collection("Users").document(userId).get().await()
            val userData = userSnapshot.data
            val userInfo = User(
                userData?.get("email") as? String ?: "",
                userId,
                userData?.get("isAdmin") as? Boolean ?: false,
                userData?.get("isSuspended") as? Boolean ?: false,
                userData?.get("isBanned") as? Boolean ?: false,
            )
            allRequestUsers.add(RequestUser(id = userId, requestLocations = requestLocations, userInfo = userInfo))
        }

        emit(allRequestUsers)
    }

    private fun sortRequests() {
        val currentUserId = AuthenticationManager.getCurrentUserId()
        _requests.value = _requests.value.sortedWith(
            compareByDescending<RequestUser> { it.userInfo.uid == currentUserId }
                .thenByDescending { it.userInfo.isAdmin }
                .thenByDescending { it.userInfo.isSuspended }
                .thenBy { it.userInfo.email }
        )
    }

    private fun sortRequestLocations(){
        _selectedUser.value?.requestLocations = _selectedUser.value?.requestLocations?.sortedWith(
            compareByDescending<RequestLocations> { !it.id.contains("NewLocation") }
                .thenBy {it.created_at}
        )!!
    }

    fun getUserColor(user: User): Color {
        return when {
            user.isAdmin && user.uid == AuthenticationManager.getCurrentUserId() -> Color(0xFF90EE90)
            user.isAdmin -> Color(0xFFADD8E6)
            user.isSuspended -> Color(0xFFFFFF00)
            else -> Color.White
        }
    }

    fun getRequestColor(request: RequestLocations): Color{
        return when {
            request.id.contains("NewLocation") -> Color(0xFF90EE90)
            else -> Color(0xFFADD8E6)
        }
    }

    private fun getLocations(): Flow<List<GeoPoint>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("Locations").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                GeoPoint(
                    document.getGeoPoint("coordinates")!!.latitude,
                    document.getGeoPoint("coordinates")!!.longitude
                )
            }
        }
    }

    fun removeRequest(user: RequestUser, request: RequestLocations){
        _isLoading.value = true
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
                    }catch (e: Exception){
                        Log.e("DeleteRequestError", "Error deleting proof image: ${e.message}", e)
                        _errorMessage.value = e.message ?: "\"Error Removing Request \""
                    }
                }
                // If successful remove the request from the user locally so no fetching is necessary
                removeRequestFromUserLocally(user, request)

            }catch (e: Exception){
                Log.e("DeleteRequestError", "Error deleting request: ${e.message}", e)
                _errorMessage.value = e.message ?: "\"Error Removing Request \""
            }

        }
    }

    private fun removeRequestFromUserLocally(user: RequestUser, request: RequestLocations) {
        _requests.value = _requests.value.map {requestUser ->
            if(requestUser.id == user.id){
                requestUser.copy(requestLocations = requestUser.requestLocations.filterNot { it.id == request.id })
            }else{
                requestUser
            }
        }
        // Remove selection
        _selectedRequest.value = null
        _selectedUser.value = null
        _isSuccess.value = true
    }

    fun addLocationAndApproveRequest(user: RequestUser, request: RequestLocations,newId: String){
        _isLoading.value = true

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
                        _errorMessage.value = e.message ?: "\"Error Adding Entry \""
                    }
                approveRequest(user, request,newId)
            }catch (e: Exception){
                Log.e("AddLocationAndApproveRequest", "Error adding Location: ${e.message}", e)
                _errorMessage.value = e.message ?: "\"Error Adding Entry \""

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
                    "last_updated" to request.created_at
                )
                locationsCollection.set(entryData)
                    .addOnSuccessListener {
                        Log.d("ApproveRequest", "Item updated/added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ApproveRequest", "Error updating/adding item: ${e.message}", e)
                        _errorMessage.value = e.message ?: "\"Error Adding Entry \""
                    }
            } catch (e: Exception) {
                Log.e("ApproveRequest", "Error updating item: ${e.message}", e)
                _errorMessage.value = e.message ?: "\"Error Adding Entry \""
            }
            removeRequest(user, request)
        }
    }
}