package com.example.monsterfindrapp.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.viewModel.ItemsViewModel


@Composable
fun ItemsScreen(navController: NavController, viewModel: ItemsViewModel) {
    var searchText by remember { mutableStateOf("") }

    val filteredItems by viewModel.getFilteredItems(searchText).collectAsState(initial = emptyList())

    //val items by viewModel.monsterItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { androidx.compose.material.Text("Search") }
            )
            Button(onClick = { viewModel.showAddItemModal() }) {
                Text("Add")
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
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
                        .size(200.dp)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Column{
                    Text(text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = item.description)
                }
            }
            Button(onClick = { viewModel.removeItem(item) }) {
                Text("Remove")
            }
        }
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

                Button(onClick = {
                    viewModel.checkAndRequestPermission(context,requestPermissionLauncher)
                },
                    shape = CircleShape,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Pick Image")
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
                if (showError) {
                    Text(text = "Please fill in all fields", color = Color.Red)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (itemName.isEmpty() || itemDescription.isEmpty() || selectedImageUri == null) {
                        showError = true
                    } else {
                        selectedImageUri?.let { onSubmit(itemName, itemDescription, it) }
                        onDismiss()
                    }
                }) {
                    Text("Submit")
                }
            }
        }
    }
}

@Composable
fun rememberPermissionLauncher(
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
fun rememberPickImageLauncher(
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
