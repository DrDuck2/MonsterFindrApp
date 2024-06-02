package com.example.monsterfindrapp.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.monsterfindrapp.utility.RequestsRepository
import com.example.monsterfindrapp.viewModel.RequestsViewModel

@Composable
fun RequestsScreen(navController: NavController, viewModel: RequestsViewModel) {

    //On back return to the main screen
    BackHandler {
        navController.navigate("AdminDashboardScreen")
    }

    val requests = RequestsRepository.requests.collectAsState()


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
                        onCardClick = {
                            viewModel.selectUser(request,request.requestLocations)
                            navController.navigate("RequestsScreenRequests")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}

