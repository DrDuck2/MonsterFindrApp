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
import com.example.monsterfindrapp.viewModel.StoreItemsViewModel

@Composable
fun StoreItemsScreen(navController: NavController, viewModel: StoreItemsViewModel){

    val isLoading by LoadingStateManager.isLoading.collectAsState()

    val selectedLocation = viewModel.selectedLocation.collectAsState()

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
        selectedLocation.value?.let {
            items(it.items) { item ->
                StoreItemCard(item, viewModel)
            }
        }
    }
}



