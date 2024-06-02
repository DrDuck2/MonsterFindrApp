package com.example.monsterfindrapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel

@Composable
fun HandleNotificationScreen(navController: NavController, viewModel: HandleNotificationViewModel){

    val userNotifications = viewModel.userNotifications.collectAsState()

    val isLoading by LoadingStateManager.isLoading.collectAsState()

    var showNotification by remember { mutableStateOf(false) }

    if(isLoading){
        LoadingOverlay(
            onDismiss = {
                LoadingStateManager.resetLoading()
            }
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
    ) {
        // Add a button for creating new notifications
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))
        {
            Button(
                onClick = {
                    showNotification = true
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
                    imageVector = Icons.Filled.NotificationAdd,
                    contentDescription = "Create Notification",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Add a LazyColumn to display the notification cards
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(userNotifications.value) {notification ->
                NotificationCard(notification, viewModel)
            }
        }
    }
    if(showNotification){
        NotificationModal(
            onDismiss = {showNotification = false},
            onSelect = {
                viewModel.submitNotification()
                showNotification = false
            },
            viewModel
        )
    }
}

@Composable
fun NotificationModal(
    onDismiss: () -> Unit,
    onSelect: () -> Unit,
    viewModel: HandleNotificationViewModel
){
    val selectedDrink by viewModel.selectedDrink.collectAsState(null)

    var expandDrinkType by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }


    Dialog(onDismissRequest = onDismiss){
        Surface(color = MaterialTheme.colorScheme.background){
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text("Create New Notification", style = MaterialTheme.typography.headlineSmall)
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
                                    .background(androidx.compose.material.MaterialTheme.colors.surface)
                            ) {
                                Column {
                                    LazyColumn(
                                        modifier = Modifier
                                            .width(276.dp)
                                            .height(200.dp)
                                            .background(androidx.compose.material.MaterialTheme.colors.surface)
                                    ) {
                                        items(viewModel.filterMonsterItems()) { type ->
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
                Spacer(modifier = Modifier.height(12.dp))
                if(showError){
                    Text("Please select a drink type", color = Color.Red)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = {
                    if(selectedDrink == null){
                        showError = true
                    }else{
                        onSelect()
                    }
                }){
                    Text("Submit")
                }
            }
        }
    }
}
