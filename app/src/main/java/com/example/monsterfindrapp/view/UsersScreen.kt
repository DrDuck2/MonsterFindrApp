package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.viewModel.UsersViewModel

@Composable
fun UsersScreen(navController: NavController, viewModel: UsersViewModel) {
    var searchText by remember { mutableStateOf("") }
    val users = listOf(
        "User 1",
        "User 2",
        "User 3"
    )
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search") }
        )
        LazyColumn {
            items(users) { user ->
                UserCard(user)
            }
        }
    }
}

@Composable
fun UserCard(user: String) {
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
            Text(text = user)
            Row {
                Button(onClick = { /* Handle suspend */ }) {
                    Text("Suspend")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { /* Handle ban */ }) {
                    Text("Ban")
                }
            }
        }
    }
}