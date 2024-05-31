package com.example.monsterfindrapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.viewModel.RequestsViewModel
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
fun RequestsScreenDetails(navController: NavController, viewModel: RequestsViewModel) {

    val selectedRequest = viewModel.selectedRequest.collectAsState()
    val selectedUser = viewModel.selectedUser.collectAsState()
    val mapLocations by viewModel.locations.collectAsState()

    var showLocation by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Request details
        Text("ID: ${selectedRequest.value?.id}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Item: ${selectedRequest.value?.item}", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Price: ${selectedRequest.value?.price}", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Availability: ${selectedRequest.value?.availability}", fontSize = 18.sp)
        Column {
            Text(
                "Coordinates: ${selectedRequest.value?.coordinates?.latitude}, ${selectedRequest.value?.coordinates?.longitude}",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    showLocation = true
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White
                )
            }

        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(300.dp)
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
            Image(
                painter = rememberAsyncImagePainter(selectedRequest.value?.imageProof),
                contentDescription = "Proof Image",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom side buttons
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (selectedRequest.value!!.id.contains("NewLocation")) {
                        showDialog = true
                    } else {
                        viewModel.approveRequest(
                            user = selectedUser.value!!,
                            request = selectedRequest.value!!,selectedRequest.value!!.id,
                            navController)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Approve")
            }
            Button(
                onClick = {
                    viewModel.removeRequest(selectedUser.value!!, selectedRequest.value!!, navController)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Remove")
            }
        }
    }
    if(showLocation){
        Dialog(onDismissRequest = { showLocation = false }) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable { showLocation = false }
            ) {
                // Context for the map
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(selectedRequest.value!!.coordinates.latitude, selectedRequest.value!!.coordinates.longitude), 15f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = MapType.NORMAL,
                        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.custom_map_style_full)
                    ),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true),
                ){
                    mapLocations.forEach{ location ->
                        val position = LatLng(location.latitude, location.longitude)
                        val icon = if(selectedRequest.value!!.coordinates == location){
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        }else{
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        }
                        Marker(
                            state = MarkerState(position = position),
                            icon = icon
                        )
                    }
                    if(selectedRequest.value!!.id.contains("NewLocation")){
                        val position = LatLng(selectedRequest.value!!.coordinates.latitude, selectedRequest.value!!.coordinates.longitude)
                        val icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        Marker(
                            state = MarkerState(position = position),
                            icon = icon
                        )
                    }
                }
            }
        }
    }
    StoreNameDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false},
        onSubmit = {
            viewModel.addLocationAndApproveRequest(user = selectedUser.value!!, request = selectedRequest.value!!,it, navController)
        }
    )
}

@Composable
fun StoreNameDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val storeNameState = remember { mutableStateOf(TextFieldValue()) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Enter Store Name")
            },
            text = {
                Column {
                    TextField(
                        value = storeNameState.value,
                        onValueChange = { storeNameState.value = it },
                        label = { Text(text = "Store Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmit(storeNameState.value.text)
                        onDismiss()
                    }
                ) {
                    Text(text = "Submit")
                }
            },
            dismissButton = null
        )
    }
}



