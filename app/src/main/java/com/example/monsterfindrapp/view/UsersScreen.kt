package com.example.monsterfindrapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.User
import com.example.monsterfindrapp.viewModel.ItemsViewModel
import com.example.monsterfindrapp.viewModel.LoginRegisterViewModel
import com.example.monsterfindrapp.viewModel.UsersViewModel
import java.text.DateFormat

@Composable
fun UsersScreen(navController: NavController, viewModel: UsersViewModel) {
    var searchText by remember { mutableStateOf("") }
    //val users by viewModel.user.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("") }
    val filteredUsers by viewModel.getFilteredUsers(searchText, selectedFilter).collectAsState(initial = emptyList())

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
                label = { Text("Search") },
                modifier = Modifier
                    .weight(0.5f)
            )
            Row {
                if (selectedFilter.isNotEmpty()) {
                    ClickableText(
                        text = AnnotatedString("$selectedFilter x"),
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(start = 16.dp),
                        onClick = {
                            selectedFilter = ""
                        },
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Box{
                    IconButton(onClick = {
                        expanded = true
                    }) {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = "Filter")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            selectedFilter = "Suspended"
                        }) {
                            Text("Suspended")
                        }
                        DropdownMenuItem(onClick = {
                            expanded = false
                            selectedFilter = "Admin"
                        }) {
                            Text("Admin")
                        }
                        DropdownMenuItem(onClick = {
                            expanded = false
                            selectedFilter = "Regular"
                        }) {
                            Text("Regular")
                        }
                    }
                }
            }
        }
        LazyColumn {
            items(filteredUsers) { user ->
                UserCard(user, viewModel)
            }
        }
    }
}

@Composable
fun UserCard(user: User, viewModel: UsersViewModel) {
    val suspendDate by viewModel.suspendDate.observeAsState()

    var userWork by remember{ mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        viewModel.callGetSuspendDate(user)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = viewModel.getUserColor(user)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ){
                Text(text = "Email: " + user.email, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Uid: " + user.uid, fontSize = 8.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if(user.isSuspended){
                    Text(text = "Suspend Date: ${suspendDate?.let {
                        DateFormat.getDateInstance().format(
                            it
                        )
                    }}")
                }
            }
            if(!user.isAdmin){
                if(!user.isSuspended){
                    Column(
                        modifier = Modifier
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Button(
                            onClick = {
                                userWork = "Suspend User"
                                showDialog = true
                                //viewModel.callSuspendUser(user)
                            },
                            modifier = Modifier
                                .size(60.dp, 50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Suspend User",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                userWork = "Ban User"
                                showDialog = true
                                //viewModel.callBanUser(user)
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
                                imageVector = Icons.Filled.BackHand,
                                contentDescription = "Ban User",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }else{
                    Column(
                        modifier = Modifier
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Button(
                            onClick = {
                                userWork = "Un Suspend User"
                                showDialog = true
                                //viewModel.callUnSuspendUser(user)
                            },
                            modifier = Modifier
                                .size(60.dp, 50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Start,
                                contentDescription = "Manual Un Suspend",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                userWork = "Ban User"
                                showDialog = true
                                // viewModel.callBanUser(user)
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
                                imageVector = Icons.Filled.BackHand,
                                contentDescription = "Ban User",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

            }else{
                if(user.uid == AuthenticationManager.getCurrentUserId()){
                    Text(text ="ADMIN (YOU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                }else{
                    Text(text ="ADMIN",fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
    if(showDialog){
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(userWork)
            },
            text = {
                Column{
                    Text("Are you sure you want to: $userWork")
                    if(userWork == "Suspend User"){
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("(User will be Suspended for one week)")
                    }else if(userWork == "Ban User"){
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("(You won't be able to revert this action")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (userWork) {
                            "Suspend User" -> viewModel.callSuspendUser(user)
                            "Ban User" -> viewModel.callBanUser(user)
                            "Un Suspend User" -> viewModel.callUnSuspendUser(user)
                        }
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
fun LoadingOverlay(viewModel: UsersViewModel) {
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