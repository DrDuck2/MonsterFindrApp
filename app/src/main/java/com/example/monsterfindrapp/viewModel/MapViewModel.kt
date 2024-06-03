package com.example.monsterfindrapp.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.model.Locations
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val selectedLocation = mutableStateOf<Locations?>(null)
    val isMapExpanded = mutableStateOf(true)


    fun updateSearchQuery(query: String){
        _searchQuery.value = query
    }

}