package com.example.monsterfindrapp.view

import android.app.Notification
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel

@Composable
fun HandleNotificationScreen(navController: NavController, viewModel: HandleNotificationViewModel){
    val notifications = listOf(
        "Notification 1",
        "Notification 2",
        "Notification 3"
    )
    Column(modifier = Modifier.fillMaxSize()) {
        // Add a button for creating new notifications
        Button(onClick = {viewModel.showNotificationModal()}) {
            Text("Create Notification")
        }

        // Add a LazyColumn to display the notification cards
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
    if(viewModel.showNotificationModal){
        NotificationModal(
            onDismiss = {viewModel.hideNotificationModal()},
            onSelect = { ->
                viewModel.selectDrink()
            }
        )
    }
}

@Composable
fun NotificationCard(notification: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = notification,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}

@Composable
fun NotificationModal(
    onDismiss: () -> Unit,
    onSelect: () -> Unit,
){
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
                Button(onClick = onSelect) {
                    Text("Select Drink")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss){
                    Text("Submit")
                }
            }
        }
    }
}
