package com.example.monsterfindrapp.view

import android.app.Activity
import android.content.Context
import android.content.Intent
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
    var location by remember { mutableStateOf("") }
    var drinkType by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Moderate") }
    var price by remember { mutableStateOf("") }

    var expandAvailability by remember { mutableStateOf(false)}
    var expandDrinkType by remember {mutableStateOf(false)}
    val drinkTypes by viewModel.monsterItems.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.observeAsState()

    val context = LocalContext.current
    val requestPermissionLauncher = rememberPermissionLauncher(viewModel, context)
    val pickImageLauncher = rememberPickImageLauncher(viewModel)

    LaunchedEffect(Unit) {
        viewModel.initializeLaunchers(pickImageLauncher)
    }

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){

        TextField(
            value = location,
            onValueChange = {
                location = it },
            label = {Text("Location")}
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){
            Box(
                contentAlignment = Alignment.CenterEnd
            ){
                TextField(
                    value = drinkType,
                    onValueChange = {drinkType = it},
                    label = {Text("Drink Type")},
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
                                            drinkType = type.name
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
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){
            Box(
                contentAlignment = Alignment.CenterEnd
            ){
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
            onValueChange = {price = it},
            label = {Text("Price")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
            leadingIcon = {
                Text(
                    text = "€",
                    style = TextStyle(
                        fontSize = 18.sp,
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.checkAndRequestPermission(context,requestPermissionLauncher)
        },
            shape = CircleShape,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Select Picture")
        }
        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.popBackStack()
        }){
            Text("Submit Entry")
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
