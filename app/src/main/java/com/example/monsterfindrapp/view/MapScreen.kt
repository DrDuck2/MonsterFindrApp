package com.example.monsterfindrapp.view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.SideMenuItem
import com.example.monsterfindrapp.model.StoreItem
import com.example.monsterfindrapp.viewModel.ItemsViewModel
import com.example.monsterfindrapp.viewModel.MapViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.CameraUpdateFactory


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {
    val context = LocalContext.current

    var showSideMenu by remember { mutableStateOf(false) }

    val mapMenuItems = listOf(
        SideMenuItem("Set Notification") { navController.navigate("HandleNotificationScreen") },
        SideMenuItem("Request Entry") { navController.navigate("RequestEntryScreen") },
        SideMenuItem("Logout") {
            if (viewModel.logout()) navController.navigate("LoginRegisterScreen")
        }
    )
    val mapMarkers by viewModel.locations.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(45.55111 ,18.69389), 15f)
    }
    val selectedLocation by viewModel.selectedLocation
    val isMapExpanded by viewModel.isMapExpanded

    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Box(
            modifier = Modifier
                .height(80.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ){
                    viewModel.isMapExpanded.value = true
                }
        ) {
            ScreenHeader(
                showSideMenu = showSideMenu,
                onMenuClick = { showSideMenu = !showSideMenu },
                navController = navController,
                menuItems = mapMenuItems
            )
        }
        Row(
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
                mapMarkers.forEach{ storeLocation ->
                    val position = LatLng(storeLocation.location.latitude, storeLocation.location.longitude)
                    Marker(
                        state = rememberMarkerState(position = position),
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
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.5f)
                        .padding(16.dp)
                ) {
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

@Composable
fun LocationCard(item: StoreItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(item.monsterItem.imageUrl)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build()
                    ),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(150.dp)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Column{
                    Text(text = "Name: " +item.monsterItem.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Price: " +item.price.toString(),
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Availability: " +item.availability)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = item.monsterItem.description)
                    Spacer(modifier = Modifier.padding(20.dp))
                    Text(text = "Last Update: " +item.lastUpdated.toString(),
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
