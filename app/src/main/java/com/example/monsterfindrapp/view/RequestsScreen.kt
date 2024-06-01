package com.example.monsterfindrapp.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.model.RequestUser
import com.example.monsterfindrapp.viewModel.RequestsViewModel

@Composable
fun RequestsScreen(navController: NavController, viewModel: RequestsViewModel) {


    val requests = viewModel.requests.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    Text(
                        text = "Users",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
            )
        },
        content = { paddingValues ->
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
                            viewModel.selectUser(request)
                            navController.navigate("RequestsScreenRequests")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

            }
        }
    )
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
                    Text(
                        text = "ADMIN (YOU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body1.fontSize
                    )

                } else {
                    Text(
                        text = "ADMIN", fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body1.fontSize
                    )
                }
            }
        }
    }
}
