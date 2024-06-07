package com.example.monsterfindrapp.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.MapLocationsRepository
import com.example.monsterfindrapp.utility.PermissionLocationHandler
import com.example.monsterfindrapp.viewModel.StoreItemsViewModel


@Composable
fun StoresScreen(navController: NavController, viewModel: StoreItemsViewModel, permissionLocationHandler: PermissionLocationHandler) {

    AuthenticationManager.navigateOnLoginFault(navController)
    AuthenticationManager.navigateOnAdminFault(navController)

    val isLoading by LoadingStateManager.isLoading.collectAsState()

    var searchText by remember { mutableStateOf("") }

    var showAddStoreModal by remember{ mutableStateOf(false) }

    val locations by MapLocationsRepository.getFilteredStoreLocations(searchText).collectAsState(initial = emptyList())


    if(isLoading){
        LoadingOverlay(
            onDismiss = {
                LoadingStateManager.resetLoading()
            }
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { androidx.compose.material.Text("Search") }
            )
            Button(
                onClick = {
                    showAddStoreModal = true
                },
                modifier = Modifier
                    .size(60.dp, 50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier
            .background(
                color = Color.LightGray
            )
            .padding(16.dp)
            ){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(locations) { location ->
                    StoreCard(location,viewModel) { selectedLocation ->
                        viewModel.selectLocation(selectedLocation)
                        navController.navigate("StoreItemsScreen")
                    }
                }
            }
        }
    }
    if(showAddStoreModal){
        AddStoreModal(
            onDismiss = {permissionLocationHandler.clearState()
                showAddStoreModal = false},
            onSubmit = { name, lat, long ->
                showAddStoreModal = false
                viewModel.addStore(name, lat, long)
            },
            permissionLocationHandler,
            LocalContext.current,
        )
    }
}

@Composable
fun AddStoreModal(
    onDismiss: () -> Unit,
    onSubmit: (String, Double, Double) -> Unit,
    permissionLocationHandler:PermissionLocationHandler,
    context: Context,
    ) {

    var storeName by remember { mutableStateOf("") }
    var storeLatitude by remember { mutableStateOf("") }
    var storeLongitude by remember {mutableStateOf("")}

    var showError by remember { mutableStateOf(false) }


    val locationPermissionLauncher = PermissionLocationHandler.rememberLocationPermissionLauncher(permissionLocationHandler, context)
    val location by permissionLocationHandler.location.collectAsState()
    val selectedStoreLocation by permissionLocationHandler.selectedLocation.collectAsState(null)
    selectedStoreLocation?.let { storeLocation ->
        permissionLocationHandler.setLocationText(storeLocation.name)
    }

    LaunchedEffect(location){
        if(location != null){
            storeLatitude = location!!.latitude.toString()
            storeLongitude = location!!.longitude.toString()
        }
    }


    Dialog(onDismissRequest = onDismiss) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Store", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = storeLatitude,
                    onValueChange = { storeLatitude = it },
                    label = { Text("Latitude") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = storeLongitude,
                    onValueChange = { storeLongitude = it },
                    label = { Text("Longitude") },
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (showError) {
                    Text("Please fill in all fields", color = Color.Red)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = {
                            if(storeName == "" || storeLatitude == "" || storeLongitude == ""){
                                showError = true
                            } else {
                                showError = false
                                onSubmit(storeName, storeLatitude.toDouble(), storeLongitude.toDouble())
                            }
                        },
                            modifier = Modifier
                                .size(100.dp, 50.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "Submit Entry",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                permissionLocationHandler.checkAndRequestLocationPermission(
                                    context,
                                    locationPermissionLauncher
                                )
                            },
                            modifier = Modifier
                                .size(100.dp, 50.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = "Current Location",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        SmallLoadingIconOverlay()
                    }
                }
            }
        }
    }
}
