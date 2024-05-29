package com.example.monsterfindrapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.model.RequestLocations
import com.example.monsterfindrapp.model.RequestUser
import com.example.monsterfindrapp.viewModel.RequestsViewModel


@Composable
fun RequestsScreen(navController: NavController, viewModel: RequestsViewModel) {
    var displayUsers by remember { mutableStateOf(true) } // State for card click
    var displayRequests by remember { mutableStateOf(false) }

    val requests = viewModel.requests.collectAsState()
    val selectedUser = viewModel.selectedUser.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    if(displayRequests){
                        Button(onClick = {
                            displayRequests = false
                            displayUsers = true},
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White
                            )){
                            Text("Return")
                        }
                    }
                },
            )
        },
        content = { paddingValues ->
            if (displayUsers) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(requests.value) { request ->
                        RequestUserCard(
                            request,
                            viewModel,
                            onCardClick = {
                                displayUsers = false
                                displayRequests = true
                                viewModel.selectUser(request)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else if (displayRequests) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(selectedUser.value) { locations ->
                        RequestsCard(
                            locations,
                            viewModel,
                            onCardClick = {
                                displayUsers = false
                                displayRequests = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}


@Composable
fun RequestsCard(request: RequestLocations, viewModel: RequestsViewModel, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCardClick() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Store: " + request.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Item: " + request.item
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Price: " + request.price.toString()
                )
            }
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(request.imageProof)
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build()
                ),
                contentDescription = "Item Image",
                modifier = Modifier
                    .size(100.dp)
                    .aspectRatio(1f)
            )

        }

    }
}

@Composable
fun RequestUserCard(request: RequestUser, viewModel: RequestsViewModel, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = viewModel.getUserColor(request.userInfo)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCardClick() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                androidx.compose.material.Text(
                    text = "Email: " + request.userInfo.email,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material.Text(text = "Uid: " + request.userInfo.uid)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (request.userInfo.isAdmin) {
                if (request.userInfo.uid == AuthenticationManager.getCurrentUserId()) {
                    androidx.compose.material.Text(
                        text = "ADMIN (YOU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.h6.fontSize
                    )

                } else {
                    androidx.compose.material.Text(
                        text = "ADMIN", fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.h6.fontSize
                    )
                }
            }
        }
    }
}
