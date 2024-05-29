package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel
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

@Composable
fun SelectLocationScreen(navController: NavController, viewModel: RequestEntryViewModel){

    val locations by viewModel.locations.collectAsState()

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(LatLng(45.55111 ,18.69389), 15f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.custom_map_style)
        ),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        locations.forEach { storeLocation ->
            val position = LatLng(storeLocation.location.latitude, storeLocation.location.longitude)
            Marker(
                state = MarkerState(position = position),
                title = storeLocation.name,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                onClick = {
                    viewModel.selectStoreLocation(storeLocation)
                    navController.popBackStack()
                    true
                }
            )
        }
    }

}