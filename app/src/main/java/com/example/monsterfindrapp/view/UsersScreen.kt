package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.utility.UsersRepository
import com.example.monsterfindrapp.viewModel.UsersViewModel

@Composable
fun UsersScreen(navController: NavController, viewModel: UsersViewModel) {
    var searchText by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("") }

    val filteredUsers by UsersRepository.getQueriedUsers(searchText, selectedFilter).collectAsState(initial = emptyList())

    val isLoading by LoadingStateManager.isLoading.collectAsState()

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
