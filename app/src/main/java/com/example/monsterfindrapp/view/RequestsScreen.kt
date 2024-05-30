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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.model.RequestLocations
import com.example.monsterfindrapp.model.RequestUser
import com.example.monsterfindrapp.viewModel.RequestsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun RequestsScreen(navController: NavController, viewModel: RequestsViewModel) {
    var displayUsers by remember { mutableStateOf(true) } // State for card click
    var displayRequests by remember { mutableStateOf(false) }

    val requests = viewModel.requests.collectAsState()
    val selectedUser = viewModel.selectedUser.collectAsState()
    val selectedRequest = viewModel.selectedRequest.collectAsState()

    var showLocation by remember { mutableStateOf(false) }

    val mapLocations by viewModel.locations.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    if(displayRequests){
                        Button(onClick = {
                            displayRequests = false
                            displayUsers = true
                                         },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White
                            )){
                            Text("Return")
                        }
                    }else if(!displayUsers){
                        Button(onClick = {
                            displayRequests = true
                            displayUsers = false},
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White
                            )){
                            Text("Return")
                        }
                    }
                },
            )
        },
        content = { paddingValues ->
            if (displayUsers) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(requests.value) { request ->
                        RequestUserCard(
                            request,
                            viewModel,
                            onCardClick = {
                                displayUsers = false
                                displayRequests = true
                                viewModel.selectUser(request)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else if (displayRequests) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    selectedUser.value?.let {
                        items(it.requestLocations) { locations ->
                            RequestsCard(
                                locations,
                                onCardClick = {
                                    displayUsers = false
                                    displayRequests = false
                                    viewModel.selectRequest(locations)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            else {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text("Coordinates: ${selectedRequest.value?.coordinates?.latitude}, ${selectedRequest.value?.coordinates?.longitude}", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = {
                            showLocation = true
                        },
                            shape = CircleShape,
                            modifier = Modifier
                                .size(56.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor =  MaterialTheme.colors.primary),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White
                            )
                        }

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Availability: ${selectedRequest.value?.availability}", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(300.dp)
                            .background(Color.Black.copy(alpha = 0.8f))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedRequest.value!!.imageProof),
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
                                viewModel.approveRequest()
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                        ) {
                            Text("Approve")
                        }
                        Button(
                            onClick = {
                                displayRequests = true
                                displayUsers = false
                                viewModel.removeRequest(selectedUser.value!!, selectedRequest.value!!)
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                        ) {
                            Text("Remove")
                        }
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
                                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.custom_map_style)
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
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun RequestsCard(request: RequestLocations, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCardClick() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Store: " + request.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Item: " + request.item
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Price: " + request.price.toString()
                )
            }
        }
    }
}

@Composable
fun RequestUserCard(request: RequestUser, viewModel: RequestsViewModel, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = viewModel.getUserColor(request.userInfo)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCardClick() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                androidx.compose.material.Text(
                    text = "Email: " + request.userInfo.email,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material.Text(text = "Uid: " + request.userInfo.uid)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (request.userInfo.isAdmin) {
                if (request.userInfo.uid == AuthenticationManager.getCurrentUserId()) {
                    Text(
                        text = "ADMIN (YOU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body1.fontSize
                    )

                } else {
                    Text(
                        text = "ADMIN", fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body1.fontSize
                    )
                }
            }
        }
    }
}
