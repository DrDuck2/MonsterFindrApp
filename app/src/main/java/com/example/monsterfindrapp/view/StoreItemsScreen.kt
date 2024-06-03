package com.example.monsterfindrapp.view

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.StoreItem
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.MonsterRepository
import com.example.monsterfindrapp.utility.PermissionImageHandler
import com.example.monsterfindrapp.viewModel.ItemsViewModel
import com.example.monsterfindrapp.viewModel.StoreItemsViewModel

@Composable
fun StoreItemsScreen(navController: NavController, viewModel: StoreItemsViewModel){
    AuthenticationManager.navigateOnLoginFault(navController)
    AuthenticationManager.navigateOnAdminFault(navController)

    val isLoading by LoadingStateManager.isLoading.collectAsState()

    val selectedLocation = viewModel.selectedLocation.collectAsState()

    var searchText by remember { mutableStateOf("") }

    var showAddModal by remember{ mutableStateOf(false)}

    if(isLoading){
        LoadingOverlay(
            onDismiss = {
                LoadingStateManager.resetLoading()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search") }
            )
            Button(
                onClick = {
                    showAddModal = true
                },
                modifier = Modifier
                    .size(60.dp, 50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedLocation.value?.let {
                items(it.items) { item ->
                    StoreItemCard(item, viewModel)
                }
            }
        }
    }
    if(showAddModal){
        AddStoreItemModal(
            onDismiss = { showAddModal = false },
            onSubmit = { item, price, availability ->
                showAddModal = false
                viewModel.addStoreItem(item, price, availability)
            },
            viewModel
        )
    }
}

@Composable
fun AddStoreItemModal(onDismiss: () -> Unit,
                      onSubmit: (MonsterItem, Double, String) -> Unit,
                      viewModel: StoreItemsViewModel
) {

    var showError by remember { mutableStateOf(false) }

    val drinkTypes by MonsterRepository.monsterItems.collectAsState()

    var expandDrinkType by remember { mutableStateOf(false) }
    var expandAvailability by remember { mutableStateOf(false) }


    val selectedDrink by viewModel.selectedDrink.collectAsState(null)

    var availability by remember { mutableStateOf("Moderate") }
    var price by remember { mutableStateOf("") }



    Dialog(onDismissRequest = onDismiss,) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add/Update Item", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.CenterEnd) {
                        TextField(
                            value = if (selectedDrink == null) "" else selectedDrink!!.name,
                            onValueChange = { },
                            label = { androidx.compose.material3.Text("Drink Type") },
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
                                    .background(androidx.compose.material.MaterialTheme.colors.surface)
                            ) {
                                Column {
                                    LazyColumn(
                                        modifier = Modifier
                                            .width(276.dp)
                                            .height(200.dp)
                                            .background(androidx.compose.material.MaterialTheme.colors.surface)
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
                                                    androidx.compose.material3.Text(type.name)
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
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { androidx.compose.material3.Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        androidx.compose.material3.Text(
                            text = "€",
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.CenterEnd) {
                        androidx.compose.material3.TextField(
                            value = availability,
                            onValueChange = { availability = it },
                            label = { androidx.compose.material3.Text("Availability") },
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
                                androidx.compose.material3.Text("Low")
                            }
                            DropdownMenuItem(onClick = {
                                expandAvailability = false
                                availability = "Moderate"
                            }) {
                                androidx.compose.material3.Text("Moderate")
                            }
                            DropdownMenuItem(onClick = {
                                expandAvailability = false
                                availability = "High"
                            }) {
                                androidx.compose.material3.Text("High")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (showError) {
                    Text("Please fill in all fields", color = Color.Red)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = {
                    if(selectedDrink == null || price.isEmpty()){
                        showError = true
                    }else{
                        showError = false
                        onSubmit(selectedDrink!!, price.toDouble(), availability)
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
                        contentDescription = "Submit Entry",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}



