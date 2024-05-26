package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.viewModel.RequestsViewModel

@Composable
fun RequestsScreen(navController: NavController, viewModel: RequestsViewModel) {
    val requests = listOf(
        "Request 1",
        "Request 2",
        "Request 3"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(requests) { request ->
                RequestCard(request)
            }
        }
    }
}

@Composable
fun RequestCard(request: String) {
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
            Text(text = request)
            Row {
                Button(onClick = { /* Handle approve */ }) {
                    Text("Approve")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { /* Handle remove */ }) {
                    Text("Remove")
                }
            }
        }
    }
}
