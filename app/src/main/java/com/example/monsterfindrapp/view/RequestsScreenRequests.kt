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
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.viewModel.RequestsViewModel

@Composable
fun RequestsScreenRequests(navController: NavController, viewModel: RequestsViewModel) {

    AuthenticationManager.navigateOnLoginFault(navController)
    AuthenticationManager.navigateOnAdminFault(navController)


    val userInfo = viewModel.usersRequests.collectAsState().value.collectAsState(emptyList())

    BackHandler {
        navController.navigate("RequestsScreen")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    Text(
                    text = "Requests",
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
                items(userInfo.value) { locations ->
                    RequestsCard(
                        locations!!,
                        onCardClick = {
                            viewModel.selectRequest(locations)
                            navController.navigate("RequestsScreenDetails")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}
