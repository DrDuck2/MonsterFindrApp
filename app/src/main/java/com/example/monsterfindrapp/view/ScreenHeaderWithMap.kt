package com.example.monsterfindrapp.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.utility.MapLocationsRepository
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.SideMenuItem
import com.example.monsterfindrapp.model.StoreItem
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
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.Flow

@Composable
fun ScreenHeaderWithMap(
    navController: NavController,
    viewModel: MapViewModel,
    menuItems: List<SideMenuItem>,
    isMapExpanded: Boolean,
    onMarkerClick: (Locations, Flow<List<StoreItem>>) -> Unit,
    onMapClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()

    val locations by MapLocationsRepository.getFilteredLocations(searchQuery).collectAsState(initial = emptyList())
    val locationItems = viewModel.locationItems.collectAsState().value.collectAsState(emptyList())


    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(45.55111, 18.69389), 15f)
    }

    var showSideMenu by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier
                .height(80.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onMapClick()
                }
        ) {
            ScreenHeader(
                showSideMenu = showSideMenu,
                onMenuClick = { showSideMenu = !showSideMenu },
                navController = navController,
                menuItems = menuItems,
                searchQuery = searchQuery,
                onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) }
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            GoogleMap(
                modifier = if (isMapExpanded) Modifier.fillMaxSize() else Modifier.weight(0.5f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.custom_map_style)
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                onMapClick = { onMapClick() }
            ) {
                locations.forEach { storeLocation ->
                    val position = LatLng(storeLocation.location.latitude, storeLocation.location.longitude)
                    Marker(
                        state = MarkerState(position = position),
                        title = storeLocation.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            onMarkerClick(storeLocation, storeLocation.items)
                            true
                        }
                    )
                }
            }
            if (!isMapExpanded && locationItems != emptyList<StoreItem>()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f)
                        .padding(16.dp)
                ) {
                     items(locationItems.value) {item ->
                         LocationCard(item!!)
                     }
                }
            }
        }
    }
}
