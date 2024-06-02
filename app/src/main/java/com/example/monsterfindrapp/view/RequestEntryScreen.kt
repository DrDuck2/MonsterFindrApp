package com.example.monsterfindrapp.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel
import kotlinx.coroutines.delay

@Composable
fun RequestEntryScreen(navController: NavController, viewModel: RequestEntryViewModel){

    if(!AuthenticationManager.isUserAuthenticated()) {
        navController.navigate("LoginRegisterScreen")
    }

    // Setting text for the picked Location ( from the map or current )
    val locationText by viewModel.locationText.collectAsState("")

    // Locations, Location (just lat and long (of type: Location )) ,MonsterItem, Availability, Price, Image Uri
    val selectedStoreLocation by viewModel.selectedLocation.collectAsState(null)
    val location by viewModel.location.collectAsState(null)
    val selectedDrink by viewModel.selectedDrink.collectAsState(null)
    var availability by remember { mutableStateOf("Moderate") }
    var price by remember { mutableStateOf("") }
    val selectedImageUri by viewModel.selectedImageUri.observeAsState()


    // Expanding Certain DropDownMenus
    var expandAvailability by remember { mutableStateOf(false) }
    var expandDrinkType by remember { mutableStateOf(false) }

    var isError by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    val isLocationLoading by viewModel.isLocationLoading.collectAsState()
    val isLocationSuccess by viewModel.isLocationSuccess.collectAsState()
    val errorLocationMessage by viewModel.errorLocationMessage.collectAsState()

    // Fetching Drink Types from the Database
    val drinkTypes by viewModel.monsterItems.collectAsState()

    // Current Location and Image Pickers
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLocationPermissionLauncher(viewModel, context)
    val imagePermissionLauncher = rememberPermissionLauncher(viewModel, context)
    val pickImageLauncher = rememberPickImageLauncher(viewModel)

    selectedStoreLocation?.let { storeLocation ->
        viewModel.setLocationText(storeLocation.name)
    }

    LaunchedEffect(Unit) {
        viewModel.initializeLaunchers(pickImageLauncher)
    }

    if (isLoading) {
        LoadingOverlay(viewModel,navController)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = locationText,
                onValueChange = { viewModel.setLocationText(it) },
                readOnly = true,
                enabled = false,
                placeholder = { Text("Location") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(
                    onClick = {
                        navController.navigate("SelectLocationScreen")
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
                        imageVector = Icons.Filled.AddLocationAlt,
                        contentDescription = "Select Location",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(50.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.checkAndRequestLocationPermission(
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
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isLocationLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color.Black
                        )
                    }
                    if (isLocationSuccess) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Current Location",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Green
                        )
                    }
                    if (errorLocationMessage != null) {
                        Text(
                            text = "Error: $errorLocationMessage",
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.CenterEnd) {
                TextField(
                    value = if (selectedDrink == null) "" else selectedDrink!!.name,
                    onValueChange = { },
                    label = { Text("Drink Type") },
                    readOnly = true
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable(onClick = { expandDrinkType = true })
                        .height(56.dp)
                        .width(276.dp)
                )
                if (expandDrinkType) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { expandDrinkType = false },
                        modifier = Modifier
                            .width(220.dp)
                            .background(MaterialTheme.colors.surface)
                    ) {
                        Column {
                            LazyColumn(
                                modifier = Modifier
                                    .width(276.dp)
                                    .height(200.dp)
                                    .background(MaterialTheme.colors.surface)
                            ) {
                                items(drinkTypes) { type ->
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.selectDrink(type)
                                            expandDrinkType = false
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = rememberAsyncImagePainter(type.imageUrl),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .padding(end = 8.dp)
                                            )
                                            Text(type.name)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f)) // Fill remaining space
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.CenterEnd) {
                TextField(
                    value = availability,
                    onValueChange = { availability = it },
                    label = { Text("Availability") },
                    readOnly = true
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable(onClick = { expandAvailability = true })
                        .height(56.dp)
                        .width(276.dp)
                )
                DropdownMenu(
                    expanded = expandAvailability,
                    onDismissRequest = { expandAvailability = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expandAvailability = false
                        availability = "Low"
                    }) {
                        Text("Low")
                    }
                    DropdownMenuItem(onClick = {
                        expandAvailability = false
                        availability = "Moderate"
                    }) {
                        Text("Moderate")
                    }
                    DropdownMenuItem(onClick = {
                        expandAvailability = false
                        availability = "High"
                    }) {
                        Text("High")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Text(
                    text = "€",
                    style = TextStyle(fontSize = 18.sp)
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.checkAndRequestPermissionImages(context, imagePermissionLauncher)
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
                    imageVector = Icons.Filled.ImageSearch,
                    contentDescription = "Select Image",
                    modifier = Modifier.size(24.dp)
                )
            }
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface)
                )

                IconButton(
                    onClick = { viewModel.removeImageUri() },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear Image",
                        tint = Color.Red
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (isError) {
            Text("Please fill in all fields", color = Color.Red)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                if ((selectedStoreLocation == null && location == null) || selectedDrink == null || selectedImageUri == null || availability.isEmpty() || price.isEmpty()) {
                    isError = true
                } else {
                    isError = false
                    if (location == null) {
                        viewModel.submitEntry(
                            selectedStoreLocation!!,
                            selectedDrink!!,
                            availability,
                            price,
                            selectedImageUri!!
                        )
                    } else {
                        viewModel.submitEntryCurrentLocation(
                            location!!,
                            selectedDrink!!,
                            availability,
                            price,
                            selectedImageUri!!
                        )
                    }
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
                contentDescription = "Select Image",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun LoadingOverlay(viewModel: RequestEntryViewModel, navController: NavController) {
    val isSuccess by viewModel.isSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
        color = Color.Black.copy(alpha = 0.5f),

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if(errorMessage != null){
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ){
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
                viewModel.setLoading(false)
                navController.popBackStack()
            }else if(isSuccess){
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ){
                    Text(text = "Entry Submitted",
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
                viewModel.setLoading(false)
                navController.popBackStack()
            }else{
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White
                )
            }
        }
    }
}
@Composable
private fun rememberLocationPermissionLauncher(
    requestEntryViewModel: RequestEntryViewModel,
    context: Context
): ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestEntryViewModel.getCurrentLocation(context)
        } else {
            // Handle permission denial
        }
    }
}

@Composable
private fun rememberPermissionLauncher(
    requestEntryViewModel: RequestEntryViewModel,
    context: Context
): ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestEntryViewModel.launchImagePicker()
        } else {
            // Handle permission denial
        }
    }
}

@Composable
private fun rememberPickImageLauncher(
    requestEntryViewModel: RequestEntryViewModel
): ActivityResultLauncher<Intent> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                requestEntryViewModel.setImageUri(uri)
            }
        }
    }
}

