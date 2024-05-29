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
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RequestsViewModel: ViewModel() {
    private val _requests = MutableStateFlow<List<RequestUser>>(emptyList())
    val requests: StateFlow<List<RequestUser>> = _requests

    private val _selectedUser = MutableStateFlow<List<RequestLocations>>(emptyList())
    val selectedUser: StateFlow<List<RequestLocations>> = _selectedUser

    fun selectUser(user: RequestUser) {
        _selectedUser.value = user.requestLocations
    }


    init {
        viewModelScope.launch() {
            getRequests().collect { items ->
                _requests.value = items
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
}