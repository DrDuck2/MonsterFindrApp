package com.example.monsterfindrapp.view

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.model.SideMenuItem
import com.example.monsterfindrapp.viewModel.MapViewModel

@Composable
fun AdminDashboardScreen(navController: NavController, viewModel: MapViewModel) {

    AuthenticationManager.navigateOnLoginFault(navController)
    AuthenticationManager.navigateOnAdminFault(navController)

    val isMapExpanded by viewModel.isMapExpanded

    BackHandler {
        if (!isMapExpanded) {
            viewModel.isMapExpanded.value = true
        } else {
            AuthenticationManager.logout()
            navController.navigate("LoginRegisterScreen")
        }
    }

    val mapMenuItems = listOf(
        SideMenuItem("Store Items") { navController.navigate("StoresScreen")},
        SideMenuItem("Requests") { navController.navigate("RequestsScreen") },
        SideMenuItem("Users") { navController.navigate("UsersScreen") },
        SideMenuItem("Items") { navController.navigate("ItemsScreen") },
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

