package com.example.monsterfindrapp.view

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.model.SideMenuItem
import com.example.monsterfindrapp.viewModel.MapViewModel


@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {

    val isMapExpanded by viewModel.isMapExpanded

    AuthenticationManager.navigateOnLoginFault(navController)
    BackHandler {
        if (!isMapExpanded) {
            viewModel.isMapExpanded.value = true
        } else {
            AuthenticationManager.logout()
            navController.navigate("LoginRegisterScreen")
        }
    }
    val mapMenuItems = listOf(
        SideMenuItem("Set Notification") { navController.navigate("HandleNotificationScreen") },
        SideMenuItem("Request Entry") { navController.navigate("RequestEntryScreen") },
        SideMenuItem("Logout") {
            if (AuthenticationManager.logout()) navController.navigate("LoginRegisterScreen")
        }
    )

    ScreenHeaderWithMap(
        navController = navController,
        viewModel = viewModel,
        menuItems = mapMenuItems,
        isMapExpanded = isMapExpanded,
        onMarkerClick = { location, items ->
            viewModel.selectLocation(location, items)
            viewModel.isMapExpanded.value = false
        },
        onMapClick = {
            viewModel.isMapExpanded.value = true
        }
    )
}

