package com.example.monsterfindrapp.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel
import com.example.monsterfindrapp.viewModel.ItemsViewModel


@Composable
fun ItemsScreen(navController: NavController, viewModel: ItemsViewModel) {
    var searchText by remember { mutableStateOf("") }

    val filteredItems by viewModel.getFilteredItems(searchText).collectAsState(initial = emptyList())

    //val items by viewModel.monsterItems.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    if(isLoading){
        LoadingOverlay(viewModel)
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
                label = { androidx.compose.material.Text("Search") }
            )
            androidx.compose.material.Button(
                onClick = {
                    viewModel.showAddItemModal()
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
        LazyColumn {
            items(filteredItems) { item ->
                ItemCard(item, viewModel)
            }
        }

        if(viewModel.showAddItemModal){
            AddItemModal(
                onDismiss = {viewModel.hideAddItemModal()},
                onSubmit = {itemName, itemDescription, imageUrl ->
                    viewModel.uploadImageAndSaveItem(itemName, itemDescription, imageUrl)
                },
                viewModel
            )
        }
    }
}

@Composable
fun ItemCard(item: MonsterItem, viewModel: ItemsViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build()
                    ),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(50.dp)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Column{
                    Text(text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = item.description)
                }
            }
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .size(60.dp, 50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Remove Item",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                androidx.compose.material.Text("Remove Item")
            },
            text = {
                androidx.compose.material.Text("Are you sure you want to remove item?")
            },
            confirmButton = {
                androidx.compose.material.Button(
                    onClick = {
                        viewModel.removeItem(item)
                        showDialog = false // Dismiss dialog after confirmation
                    }
                ) {
                    androidx.compose.material.Text("Yes")
                }
            },
            dismissButton = {
                androidx.compose.material.Button(
                    onClick = { showDialog = false }
                ) {
                    androidx.compose.material.Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddItemModal(onDismiss: () -> Unit,
                 onSubmit: (String, String, Uri) -> Unit,
                 viewModel: ItemsViewModel) {

    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    val selectedImageUri by viewModel.selectedImageUri.observeAsState()

    val context = LocalContext.current
    val requestPermissionLauncher = rememberPermissionLauncher(viewModel, context)
    val pickImageLauncher = rememberPickImageLauncher(viewModel)

    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initializeLaunchers(pickImageLauncher)
    }

    Dialog(onDismissRequest = onDismiss,) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Item", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemDescription,
                    onValueChange = { itemDescription = it },
                    label = { Text("Description") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.checkAndRequestPermission(context,requestPermissionLauncher)
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
                                .background(androidx.compose.material.MaterialTheme.colors.surface)
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
                if (showError) {
                    Text("Please fill in all fields", color = Color.Red)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = {
                    if (itemName.isEmpty() || itemDescription.isEmpty() || selectedImageUri == null) {
                        showError = true
                    } else {
                        selectedImageUri?.let { onSubmit(itemName, itemDescription, it) }
                        onDismiss()
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
    }
}

@Composable
fun LoadingOverlay(viewModel: ItemsViewModel) {
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
                    if (errorMessage != null || isSuccess) {
                        viewModel.resetLoading()
                    }
                }),
        color = Color.Black.copy(alpha = 0.5f),

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
                    androidx.compose.material3.Text(text = "Error: $errorMessage",
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
                    androidx.compose.material3.Text(text = "Success",
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
private fun rememberPermissionLauncher(
    itemsViewModel: ItemsViewModel,
    context: Context
): ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            itemsViewModel.launchImagePicker()
        } else {
            // Handle permission denial
        }
    }
}

@Composable
private fun rememberPickImageLauncher(
    itemsViewModel: ItemsViewModel
): ActivityResultLauncher<Intent> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                itemsViewModel.setImageUri(uri)
            }
        }
    }
}
