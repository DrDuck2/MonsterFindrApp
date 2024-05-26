package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.monsterfindrapp.model.SideMenuItem
import com.example.monsterfindrapp.viewModel.MapViewModel
import com.google.android.gms.maps.MapView

@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {
    var showSideMenu by remember { mutableStateOf(false) }

    val mapMenuItems = listOf(
        SideMenuItem("Set Notification") { navController.navigate("HandleNotificationScreen") },
        SideMenuItem("Request Entry") { navController.navigate("RequestEntryScreen") },
        SideMenuItem("Logout") {
            if (viewModel.logout()) navController.navigate("LoginRegisterScreen")
        }
    )
    ScreenHeader(
        showSideMenu = showSideMenu,
        onMenuClick = { showSideMenu = !showSideMenu },
        navController = navController,
        menuItems = mapMenuItems
    )
}