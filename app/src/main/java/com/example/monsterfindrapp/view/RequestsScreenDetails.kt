package com.example.monsterfindrapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.viewModel.ItemsViewModel
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel
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
    var showWarning by remember {mutableStateOf(false)}

    var isZoomed by remember {mutableStateOf(false)}
    var scale = if (isZoomed) 2f else 1f

    val isLoading by viewModel.isLoading.collectAsState()

    var requestWork by remember {mutableStateOf("")}

    if(isLoading){
        LoadingOverlay(viewModel,navController)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Request Details",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        content = { paddingValues ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            // Request details
            Column(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "ID: ${selectedRequest.value?.id ?: "N/A"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Item: ${selectedRequest.value?.item ?: "N/A"}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Price: ${selectedRequest.value?.price ?: "N/A"}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Availability: ${selectedRequest.value?.availability ?: "N/A"}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Coordinates: ${selectedRequest.value?.coordinates?.latitude ?: "N/A"}, ${selectedRequest.value?.coordinates?.longitude ?: "N/A"}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { showLocation = true },
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp),
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
            }
            Spacer(modifier = Modifier.weight(0.2f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray)
            ) {
                Column {
                    Text(
                        text = "Image For Proof",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                    Image(
                        painter = rememberAsyncImagePainter(selectedRequest.value?.imageProof),
                        contentDescription = "Proof Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                            )
                            .clickable(onClick = {
                                isZoomed = !isZoomed
                            }),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        requestWork = "Approve Request"
                        showWarning = true
                    },
                    modifier = Modifier
                        .size(100.dp, 50.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Approve Entry",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Button(
                    onClick = {
                        requestWork = "Remove Request"
                        showWarning = true
                    },
                    modifier = Modifier
                        .size(100.dp, 50.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove Entry",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

        }
    })
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
            viewModel.addLocationAndApproveRequest(user = selectedUser.value!!, request = selectedRequest.value!!,it)
        }
    )
    if(showWarning){
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = {
                Text(requestWork)
            },
            text = {
                Column{
                    Text("Are you sure you want to: $requestWork")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if(requestWork == "Approve Request") {
                            if (selectedRequest.value!!.id.contains("NewLocation")) {
                                showDialog = true
                            } else {
                                viewModel.approveRequest(
                                    user = selectedUser.value!!,
                                    request = selectedRequest.value!!,
                                    id = selectedRequest.value!!.id
                                )
                            }
                        }else if(requestWork == "Remove Request") {
                            viewModel.removeRequest(
                                user = selectedUser.value!!,
                                request = selectedRequest.value!!
                            )
                        }
                        showWarning = false // Dismiss dialog after confirmation
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showWarning = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LoadingOverlay(viewModel: RequestsViewModel, navController: NavController) {
    val isSuccess by viewModel.isSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .clickable(interactionSource = interactionSource,
                indication = null,
                onClick = {
                    navController.navigate("RequestsScreen")
                    if (errorMessage != null || isSuccess) {
                        viewModel.resetLoading()
                    }
                }),
        color = Color.DarkGray,

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ) {
                    Text(text = "Error: $errorMessage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
                            }
                        })
                }
            } else if (isSuccess) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ) {
                    Text(text = "Success",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
                            }
                        })
                }
                navController.navigate("RequestsScreen")
                viewModel.resetLoading()
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StoreNameDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val storeNameState = remember { mutableStateOf(TextFieldValue()) }
    var showError by remember { mutableStateOf(false)}
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
                if(showError){
                    Text(text = "Please enter a store name.", color = Color.Red)
                }
                Button(
                    onClick = {
                        if(storeNameState.value.text == ""){
                            showError = true
                        }else{
                            onSubmit(storeNameState.value.text)
                            onDismiss()
                        }
                    }
                ) {
                    Text(text = "Submit")
                }
            },
            dismissButton = null
        )
    }
}



