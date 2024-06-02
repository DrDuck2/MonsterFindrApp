package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.MapLocationsRepository
import com.example.monsterfindrapp.viewModel.StoreItemsViewModel


@Composable
fun StoresScreen(navController: NavController, viewModel: StoreItemsViewModel) {

    val isLoading by LoadingStateManager.isLoading.collectAsState()

    val locations by MapLocationsRepository.getStoreLocations()
        .collectAsState(initial = emptyList())


    if(isLoading){
        LoadingOverlay(
            onDismiss = {
                LoadingStateManager.resetLoading()
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(locations) { location ->
            StoreCard(location,viewModel) { selectedLocation ->
                viewModel.setSelectedLocation(selectedLocation)
                navController.navigate("StoreItemsScreen")
            }
        }
    }
}