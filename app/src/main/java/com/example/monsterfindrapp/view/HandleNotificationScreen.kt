package com.example.monsterfindrapp.view

import android.app.Notification
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun HandleNotificationScreen(navController: NavController, viewModel: HandleNotificationViewModel){

    val userNotifications = viewModel.userNotifications.collectAsState()



    Column(modifier = Modifier.fillMaxSize()) {
        // Add a button for creating new notifications
        Button(onClick = {viewModel.showNotificationModal()}) {
            Text("Create Notification")
        }

        // Add a LazyColumn to display the notification cards
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            userNotifications.value?.let {
                items(it.notifications) { notification ->
                    NotificationCard(notification, viewModel)
                }
            }
        }
    }
    if(viewModel.showNotificationModal){
        NotificationModal(
            onDismiss = {viewModel.hideNotificationModal()},
            onSelect = {
                viewModel.submitNotification()
                viewModel.hideNotificationModal()
            },
            viewModel
        )
    }
}

@Composable
fun NotificationCard(notification: com.example.monsterfindrapp.model.Notification, viewModel: HandleNotificationViewModel) {
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
                            .data(notification.item.imageUrl)
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
                    Text(text = notification.item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = notification.item.description)
                }
            }
            Button(onClick = { viewModel.removeNotification(notification) }) {
                Text("Remove")
            }
        }
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

    val drinkTypes by viewModel.monsterItems.collectAsState()
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
                Button(onClick = onSelect){
                    Text("Submit")
                }
            }
        }
    }
}
