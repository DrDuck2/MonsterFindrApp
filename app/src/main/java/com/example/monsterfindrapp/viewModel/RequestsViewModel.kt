package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.RequestLocations
import com.example.monsterfindrapp.model.RequestUser
import com.example.monsterfindrapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RequestsViewModel: ViewModel() {
    private val _requests = MutableStateFlow<List<RequestUser>>(emptyList())
    val requests: StateFlow<List<RequestUser>> = _requests

    private val _selectedUser = MutableStateFlow<RequestUser?>(null)
    val selectedUser: StateFlow<RequestUser?> = _selectedUser

    private val _locations = MutableStateFlow<List<GeoPoint>>(emptyList())
    val locations: StateFlow<List<GeoPoint>> = _locations.asStateFlow()

    private val _selectedRequest = MutableStateFlow<RequestLocations?>(null)
    val selectedRequest: StateFlow<RequestLocations?> = _selectedRequest.asStateFlow()


    fun selectUser(user: RequestUser) {
        _selectedUser.value = user
    }

    fun selectRequest(location: RequestLocations){
        _selectedRequest.value = location
    }


    init {
        viewModelScope.launch {
            getRequests().collect { items ->
                _requests.value = items
            }
        }
        viewModelScope.launch {
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
                    imageProof = data?.get("proof_image") as? String ?: ""
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

    fun getUserColor(user: User): Color {
        return when {
            user.isAdmin && user.uid == AuthenticationManager.getCurrentUserId() -> Color(0xFF90EE90)
            user.isAdmin -> Color(0xFFADD8E6)
            user.isSuspended -> Color(0xFFFFFF00)
            else -> Color.White
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
        viewModelScope.launch {
            try{
                val db = Firebase.firestore
//                val storage = FirebaseStorage.getInstance()

                val requestsCollection = db.collection("RequestEntries").document(user.id).collection("Requests")
                val requestDoc = requestsCollection.document(request.id)
                requestDoc.delete().await()

//                val proofImageUrl = request.imageProof
//                if(proofImageUrl.isNotEmpty()){
//                    val storageReference = storage.getReferenceFromUrl(proofImageUrl)
//                    try{
//                        storageReference.delete().await()
//                    }catch (e: Exception){
//                        Log.e("DeleteRequestError", "Error deleting proof image: ${e.message}", e)
//                    }
//                }
                // If successful remove the request from the user locally so no fetching is necessary
                removeRequestFromUserLocally()

            }catch (e: Exception){
                Log.e("DeleteRequestError", "Error deleting request: ${e.message}", e)
            }

        }
    }

    private fun removeRequestFromUserLocally() {
        _selectedRequest.value = null
        _selectedUser.value = null
        viewModelScope.launch {
            getRequests().collect { items ->
                _requests.value = items
            }
        }
        viewModelScope.launch {
            getLocations().collect { locations ->
                _locations.value = locations
            }
        }

    }


    fun approveRequest(){

    }
}