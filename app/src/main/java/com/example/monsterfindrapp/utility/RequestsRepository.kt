package com.example.monsterfindrapp.utility

import androidx.compose.ui.graphics.Color
import com.example.monsterfindrapp.model.RequestLocations
import com.example.monsterfindrapp.model.RequestUser
import com.example.monsterfindrapp.model.User
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

object RequestsRepository {

    private val _requests = MutableStateFlow<List<RequestUser>>(emptyList())
    val requests: StateFlow<List<RequestUser>> = _requests.asStateFlow()


    init {
        CoroutineScope(Dispatchers.IO).launch {
            getRequests().collect{ requests ->
                _requests.value = sortRequests(requests)
            }
        }
    }

    fun getRequestColor(request: RequestLocations): Color {
        return when {
            request.id.contains("NewLocation") -> Color(0xFF90EE90)
            else -> Color(0xFFADD8E6)
        }
    }


    private fun sortRequests(requests: List<RequestUser>) : List<RequestUser> {
        val currentUserId = AuthenticationManager.getCurrentUserId()
        return requests.sortedWith(
            compareByDescending<RequestUser> { it.userInfo.uid == currentUserId }
                .thenByDescending { it.userInfo.isAdmin }
                .thenByDescending { it.userInfo.isSuspended }
                .thenBy { it.userInfo.email }
        )
    }

    private fun getRequests(): Flow<List<RequestUser>>{
        val db = FirebaseFirestore.getInstance()
        return db.collection("RequestEntries").snapshots().map{ snapshot ->
            snapshot.documents.map{ document ->
                val userId = document.id
                val requestLocations = getRequestsForUser(db,userId)
                val userInfo = getUserInfo(db, userId)
                RequestUser(
                    id = userId,
                    requestLocations = requestLocations,
                    userInfo = userInfo
                )
            }
        }
    }
    private fun getRequestsForUser(db: FirebaseFirestore, userId: String): Flow<List<RequestLocations>>{
        return db.collection("RequestEntries").document(userId).collection("Requests").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val data = document.data
                RequestLocations(
                    id = document.id,
                    coordinates = data?.get("coordinates") as? GeoPoint ?: GeoPoint(0.0, 0.0),
                    availability = data?.get("availability") as? String ?: "",
                    item = data?.get("item") as? String ?: "",
                    price = (data?.get("price") as? Number)?.toDouble() ?: 0.0,
                    imageProof = data?.get("proof_image") as? String ?: "",
                    createdAt = (data?.get("createdAt") as? Timestamp)?.toDate() ?: Date()
                )
            }
        }
    }

    private suspend fun getUserInfo(db: FirebaseFirestore, userId: String): User{
        val usersSnapshot =  db.collection("Users").document(userId).get().await()
        val data = usersSnapshot.data
        return User(
            email = data?.get("email") as? String ?: "",
            uid = userId,
            isAdmin = data?.get("isAdmin") as? Boolean ?: false,
            isSuspended = data?.get("isSuspended") as? Boolean ?: false,
            isBanned = data?.get("isBanned") as? Boolean ?: false
        )
    }
}