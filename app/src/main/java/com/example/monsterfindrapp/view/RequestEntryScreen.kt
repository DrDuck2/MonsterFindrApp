package com.example.monsterfindrapp.view

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.MonsterRepository
import com.example.monsterfindrapp.utility.PermissionImageHandler
import com.example.monsterfindrapp.utility.PermissionLocationHandler
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel

@Composable
fun RequestEntryScreen(navController: NavController, viewModel: RequestEntryViewModel, permissionLocationHandler: PermissionLocationHandler){

    // UI VALUES

    // If user is not authenticated redirect to Login Screen
    AuthenticationManager.navigateOnLoginFault(navController)
    // Fetching Drink Types from the Database
    val drinkTypes by MonsterRepository.monsterItems.collectAsState()

    // Input Data
    val selectedDrink by viewModel.selectedDrink.collectAsState(null)
    var availability by remember { mutableStateOf("Moderate") }
    var price by remember { mutableStateOf("") }

    // Expanding Certain DropDownMenus
    var expandAvailability by remember { mutableStateOf(false) }
    var expandDrinkType by remember { mutableStateOf(false) }

    // Show Error for empty fields
    var isError by remember { mutableStateOf(false) }

    // Loading For Submit Entry
    val isLoading by LoadingStateManager.isLoading.collectAsState()

    // Image Selection Handling
    val context = LocalContext.current
    val permissionImageHandler = remember { PermissionImageHandler(context, viewModel) }
    val pickImageLauncher = PermissionImageHandler.rememberPickImageLauncher(permissionImageHandler)
    val imagePermissionLauncher = PermissionImageHandler.rememberPermissionLauncher(permissionImageHandler, context)
    val selectedImageUri by permissionImageHandler.selectedImageUri.observeAsState()
    permissionImageHandler.initializeLaunchers(pickImageLauncher, imagePermissionLauncher)

    // Location Selection Handling
    val locationPermissionLauncher = PermissionLocationHandler.rememberLocationPermissionLauncher(permissionLocationHandler, context)
    val location by permissionLocationHandler.location.collectAsState()
    val locationText by permissionLocationHandler.locationText.collectAsState()
    val selectedStoreLocation by permissionLocationHandler.selectedLocation.collectAsState(null)
    selectedStoreLocation?.let { storeLocation ->
        permissionLocationHandler.setLocationText(storeLocation.name)
    }



    // UI DESIGN
    if (isLoading) {
        NavigateLoadingOverlay(
            onNavigate = {
                navController.popBackStack()
            },
            setAlpha = 0.5f
        )
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
                onValueChange = { permissionLocationHandler.setLocationText(it) },
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
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallLoadingIconOverlay()
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
                    permissionImageHandler.checkAndRequestPermissionImages()
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
                    onClick = { permissionImageHandler.removeImageUri() },
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



