package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.User
import com.example.monsterfindrapp.viewModel.UsersViewModel
import java.text.DateFormat

@Composable
fun UsersScreen(navController: NavController, viewModel: UsersViewModel) {
    var searchText by remember { mutableStateOf("") }
    //val users by viewModel.user.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("") }
    val filteredUsers by viewModel.getFilteredUsers(searchText, selectedFilter).collectAsState(initial = emptyList())

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
            Row {
                if (selectedFilter.isNotEmpty()) {
                    ClickableText(
                        text = AnnotatedString("$selectedFilter x"),
                        modifier = Modifier.padding(top = 16.dp),
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
                Text(text = "Email: " + user.email, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.body1.fontSize)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Uid: " + user.uid)
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
                    Row {
                        Button(onClick = { viewModel.callSuspendUser(user)
                        }) {
                            Text("Suspend")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.callBanUser(user) }) {
                            Text("Ban")
                        }
                    }
                }else{
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { viewModel.callUnSuspendUser(user) }) {
                            Text("Manual Un Suspend")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.callBanUser(user) }) {
                            Text("Ban")
                        }
                    }
                }

            }else{
                if(user.uid == AuthenticationManager.getCurrentUserId()){
                    Text(text ="ADMIN (YOU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.h6.fontSize
                    )

                }else{
                    Text(text ="ADMIN",fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.h6.fontSize
                    )
                }
            }
        }
    }
}