package com.example.monsterfindrapp.view

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.MonsterRepository
import com.example.monsterfindrapp.utility.PermissionImageHandler
import com.example.monsterfindrapp.viewModel.ItemsViewModel


@Composable
fun ItemsScreen(navController: NavController, viewModel: ItemsViewModel) {
    AuthenticationManager.navigateOnLoginFault(navController)
    AuthenticationManager.navigateOnAdminFault(navController)

    var searchText by remember { mutableStateOf("") }

    val monsterItems by MonsterRepository.getQueriedItems(searchText).collectAsState(initial = emptyList())

    val isLoading by LoadingStateManager.isLoading.collectAsState()

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
                label = { androidx.compose.material.Text("Search") }
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
        LazyColumn {
            items(monsterItems) { item ->
                ItemCard(item, viewModel)
            }
        }

        if(showAddModal){
            AddItemModal(
                onDismiss = {showAddModal = false},
                onSubmit = {itemName, itemDescription, imageUrl ->
                    viewModel.uploadImageAndSaveItem(itemName, itemDescription, imageUrl)
                    showAddModal = false
                },
                viewModel
            )
        }
    }
}


@Composable
fun AddItemModal(onDismiss: () -> Unit,
                 onSubmit: (String, String, Uri) -> Unit,
                 viewModel: ItemsViewModel) {

    // Image Selection Handling
    val context = LocalContext.current
    val permissionImageHandler = remember { PermissionImageHandler(context, viewModel) }
    val pickImageLauncher = PermissionImageHandler.rememberPickImageLauncher(permissionImageHandler,viewModel)
    val imagePermissionLauncher = PermissionImageHandler.rememberPermissionLauncher(permissionImageHandler, context)
    val selectedImageUri by viewModel.selectedImageUri.observeAsState()
    permissionImageHandler.initializeLaunchers(pickImageLauncher, imagePermissionLauncher)

    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }


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
                        contentDescription = "Submit Entry",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
