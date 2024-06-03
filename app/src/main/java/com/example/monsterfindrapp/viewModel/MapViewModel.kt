package com.example.monsterfindrapp.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.StoreItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

class MapViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _locationItems = MutableStateFlow<Flow<List<StoreItem?>>>(emptyFlow())
    val locationItems: StateFlow<Flow<List<StoreItem?>>> = _locationItems.asStateFlow()
    private val _selectedLocation = MutableStateFlow<Locations?>(null)

    fun selectLocation(selectedLocation: Locations,locationItems: Flow<List<StoreItem>>) {
        _locationItems.value = locationItems
        _selectedLocation.value = selectedLocation
    }

    val isMapExpanded = mutableStateOf(true)

    fun updateSearchQuery(query: String){
        _searchQuery.value = query
    }

}