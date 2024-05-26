package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.monsterfindrapp.viewModel.ItemsViewModel


@Composable
fun ItemsScreen(navController: NavController, viewModel: ItemsViewModel) {
    val items = listOf(
        "Item 1",
        "Item 2",
        "Item 3"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = { viewModel.showAddItemModal() }) {
                Text("Add")
            }
        }
        LazyColumn {
            items(items) { item ->
                ItemCard(item)
            }
        }

        if(viewModel.showAddItemModal){
            AddItemModal(
                onDismiss = {viewModel.hideAddItemModal()},
                onSubmit = {viewModel.hideAddItemModal()
                }
            )
        }
    }
}

@Composable
fun ItemCard(item: String) {
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
            Text(text = item)
            Button(onClick = { /* Handle remove */ }) {
                Text("Remove")
            }
        }
    }
}

@Composable
fun AddItemModal(onDismiss: () -> Unit,
                 onSubmit: ()-> Unit)
{
    var itemName by remember {mutableStateOf("")}
    var itemDescription by remember {mutableStateOf("")}
    var itemImage by remember {mutableStateOf("")}

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
                OutlinedTextField(
                    value = itemImage,
                    onValueChange = { itemImage = it },
                    label = { Text("Image") },
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onDismiss() }) {
                    Text("Submit")
                }
            }
        }
    }
}
