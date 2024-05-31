package com.example.monsterfindrapp.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
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
import androidx.compose.material.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel

@Composable
fun RequestEntryScreen(navController: NavController, viewModel: RequestEntryViewModel){

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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = locationText,
                onValueChange = {  viewModel.setLocationText(it) },
                label = { Text("Location") },
                readOnly = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row{
                Button(onClick = {
                    navController.navigate("SelectLocationScreen")
                }) {
                    Text("Select Location")
                }
                Button(onClick = {
                    viewModel.checkAndRequestLocationPermission(context, locationPermissionLauncher)
                }) {
                    Text("Current Location")
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
                    value = if(selectedDrink == null) "" else selectedDrink!!.name,
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
        Button(
            onClick = {
                viewModel.checkAndRequestPermissionImages(context, imagePermissionLauncher)
            },
            shape = CircleShape,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Select Picture")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if(location == null){
                viewModel.submitEntry(selectedStoreLocation!!, selectedDrink!!, availability, price, selectedImageUri!! )

            }else{
                viewModel.submitEntryCurrentLocation(location!!, selectedDrink!!, availability, price, selectedImageUri!! )
            }
        }) {
            Text("Submit Entry")
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

