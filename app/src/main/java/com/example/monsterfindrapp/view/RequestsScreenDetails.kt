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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.MapLocationsRepository
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.utility.AuthenticationManager
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
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun RequestsScreenDetails(navController: NavController, viewModel: RequestsViewModel) {

    AuthenticationManager.navigateOnLoginFault(navController)
    AuthenticationManager.navigateOnAdminFault(navController)

    val selectedRequest = viewModel.selectedRequest.collectAsState()
    val user = viewModel.user.collectAsState()

    val mapLocations by MapLocationsRepository.getGeoPoints().collectAsState(initial = emptyList())

    var showLocation by remember { mutableStateOf(false) }
    val showDialog = viewModel.showDialog.collectAsState()
    var showWarning by remember {mutableStateOf(false)}

    var isZoomed by remember {mutableStateOf(false)}
    val scale = if (isZoomed) 2f else 1f
    var requestWork by remember {mutableStateOf("")}

    val isLoading by LoadingStateManager.isLoading.collectAsState()


    if(isLoading){
        NavigateLoadingOverlay(
            onNavigate = {
                navController.navigate("RequestsScreenRequests")
            },
            setAlpha = 1f
        )
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
                    val latitude = selectedRequest.value?.coordinates?.latitude ?: Double.NaN
                    val roundedLatitude = BigDecimal(latitude).setScale(4, RoundingMode.HALF_UP).toDouble()
                    val longitude = selectedRequest.value?.coordinates?.longitude ?: Double.NaN
                    val roundedLongitude= BigDecimal(longitude).setScale(4, RoundingMode.HALF_UP).toDouble()

                    val coordinatesText = "$roundedLatitude, $roundedLongitude"
                    Text(
                        text = "Coordinates: $coordinatesText",
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
        showDialog = showDialog.value,
        onDismiss = { viewModel.setShowDialog(false)},
        onSubmit = {
            viewModel.addLocationAndApproveRequest(user =  user.value!!, request = selectedRequest.value!!,it)
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
                                viewModel.checkLocation(user =  user.value!!, request = selectedRequest.value!!)
                            } else {
                                viewModel.approveRequest(
                                    user = user.value!!,
                                    request = selectedRequest.value!!,
                                    id = selectedRequest.value!!.id
                                )
                            }
                        }else if(requestWork == "Remove Request") {
                            viewModel.removeRequest(
                                user = user.value!!,
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



