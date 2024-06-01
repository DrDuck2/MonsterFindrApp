package com.example.monsterfindrapp.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.model.SideMenuItem
import com.example.monsterfindrapp.viewModel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun AdminDashboardScreen(navController: NavController, viewModel: MapViewModel) {

    val isMapExpanded by viewModel.isMapExpanded

    if(!AuthenticationManager.isUserAuthenticated()) {
        navController.navigate("LoginRegisterScreen")
    }else{
        BackHandler {
            if(!isMapExpanded){
                viewModel.isMapExpanded.value = true
            }else{
                viewModel.logout()
                navController.navigate("LoginRegisterScreen")
            }
        }
    }

        // If user not logged in


    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(45.55111 ,18.69389), 15f)
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredLocations by viewModel.getFilteredLocations(searchQuery).collectAsState(initial = emptyList())

    val interactionSource = remember { MutableInteractionSource() }

    var showSideMenu by remember { mutableStateOf(false) }

    val selectedLocation by viewModel.selectedLocation

    val mapMenuItems = listOf(
        SideMenuItem("Requests") { navController.navigate("RequestsScreen") },
        SideMenuItem("Users") { navController.navigate("UsersScreen") },
        SideMenuItem("Items") { navController.navigate("ItemsScreen") },
        SideMenuItem("Set Notification") { navController.navigate("HandleNotificationScreen") },
        SideMenuItem("Request Entry") { navController.navigate("RequestEntryScreen") },
        SideMenuItem("Logout") {
            if (viewModel.logout()) navController.navigate("LoginRegisterScreen")
        }
    )

    Column {
        Box(
            modifier = Modifier
                .height(80.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    viewModel.isMapExpanded.value = true
                }
        ) {
            ScreenHeader(
                showSideMenu = showSideMenu,
                onMenuClick = { showSideMenu = !showSideMenu },
                navController = navController,
                menuItems = mapMenuItems,
                searchQuery = searchQuery,
                onSearchQueryChange = {query -> viewModel.updateSearchQuery(query)}
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
        ){
            GoogleMap(
                modifier = if (isMapExpanded) Modifier.fillMaxSize() else Modifier.weight(0.5f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context,R.raw.custom_map_style)
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                onMapClick = {
                    viewModel.isMapExpanded.value = true
                }
            ){
                filteredLocations.forEach{ storeLocation ->
                    val position = LatLng(storeLocation.location.latitude, storeLocation.location.longitude)
                    Marker(
                        state = MarkerState(position),
                        title = storeLocation.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            viewModel.selectedLocation.value = storeLocation
                            viewModel.isMapExpanded.value = false
                            cameraPositionState.move(CameraUpdateFactory.newLatLng(position))
                            true
                        }
                    )
                }

            }
            if (!isMapExpanded && selectedLocation != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = selectedLocation!!.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box{
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(selectedLocation!!.items) { item ->
                                LocationCard(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

