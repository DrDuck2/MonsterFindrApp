package com.example.monsterfindrapp.utility

import androidx.compose.ui.graphics.Color
import com.example.monsterfindrapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object UsersRepository {
    private val _user = MutableStateFlow<List<User>>(emptyList())
    val user: StateFlow<List<User>> = _user.asStateFlow()


    init {
        CoroutineScope(Dispatchers.IO).launch {
            getUsers().collect{ users ->
                _user.value = sortUsers(users)
            }
        }
    }

    private fun getUsers(): Flow<List<User>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("Users").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val data = document.data
                User(
                    data?.get("email") as? String ?: "",
                    document.id,
                    data?.get("isAdmin") as? Boolean ?: false,
                    data?.get("isSuspended") as? Boolean ?: false,
                    data?.get("isBanned") as? Boolean ?: false,
                )
            }
        }
    }

    fun getQueriedUsers(query: String, selectedFilter: String): Flow<List<User>> {
        return if (query.isEmpty() && (selectedFilter.isEmpty() || selectedFilter == "All")) {
            _user.asStateFlow()
        } else {
            _user.asStateFlow().map { users ->
                var filteredUsers = users
                if (selectedFilter.isNotEmpty()) {
                    filteredUsers = filteredUsers.filter { user ->
                        when (selectedFilter) {
                            "Suspended" -> user.isSuspended
                            "Admin" -> user.isAdmin
                            "Regular" -> !(user.isAdmin)
                            else -> true
                        }
                    }
                }
                if (query.isNotEmpty()) {
                    filteredUsers = filteredUsers.filter { user ->
                        user.email.contains(query, ignoreCase = true) ?: false ||
                                user.uid.contains(query, ignoreCase = true)
                    }
                }
                filteredUsers
            }
        }
    }

    fun getUserColor(user: User): Color {
        return when {
            user.isAdmin && user.uid == AuthenticationManager.getCurrentUserId() -> Color(0xFF90EE90)
            user.isAdmin -> Color(0xFFADD8E6)
            user.isSuspended -> Color(0xFFFFFF00)
            else -> Color.White
        }
    }

    private fun sortUsers(users: List<User>): List<User> {
        val currentUserId = AuthenticationManager.getCurrentUserId()
        return users.sortedWith(
            compareByDescending<User> { it.uid == currentUserId }
                .thenByDescending { it.isAdmin }
                .thenByDescending { !it.isSuspended }
                .thenBy { it.email }
        )
    }


}