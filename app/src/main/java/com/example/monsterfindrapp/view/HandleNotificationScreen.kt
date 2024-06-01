package com.example.monsterfindrapp.view

import android.app.Notification
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Remove
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
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel

@Composable
fun HandleNotificationScreen(navController: NavController, viewModel: HandleNotificationViewModel){

    val userNotifications = viewModel.userNotifications.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    val restriction by viewModel.restriction.collectAsState()

    if(restriction){
        RestrictionOverlay(viewModel)
    }

    if(isLoading){
        LoadingOverlay(viewModel)
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
                    viewModel.showNotificationModal()
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
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
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
                        .size(50.dp)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Column{
                    Text(text = notification.item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .size(60.dp, 30.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Remove Notification",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Remove Notification")
            },
            text = {
                Text("Are you sure you want to remove the notification?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeNotification(notification)
                        showDialog = false // Dismiss dialog after confirmation
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
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

@Composable
fun LoadingOverlay(viewModel: HandleNotificationViewModel) {
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
            if(errorMessage != null){
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ){
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
            }else if(isSuccess){
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ){
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
            }else{
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RestrictionOverlay(viewModel: HandleNotificationViewModel) {

    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .clickable(interactionSource = interactionSource,
                indication = null,
                onClick = {
                    viewModel.setRestriction(false)
                }),
        color = Color.Black.copy(alpha = 0.5f),

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                Text(text = "This feature is not fully implemented yet, you can use it only restrictively",
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
                        }
                    })
            }
        }
    }
}
