package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.model.SideMenuItem
import com.google.android.gms.maps.MapView

@Composable
fun ScreenHeader(
    showSideMenu: Boolean,
    onMenuClick: () -> Unit,
    navController: NavController,
    menuItems: List<SideMenuItem>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = "",
                onValueChange = {},
                label = { Text("Search") }
            )
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Side Menu")
                }
                SideMenuDropdown(
                    expanded = showSideMenu,
                    onDismissRequest = { onMenuClick() },
                    navController = navController,
                    menuItems = menuItems
                )
            }
        }
        //Spacer(modifier = Modifier.weight(1f))
        //MapView(LocalContext.current).apply {
        //    onCreate(null)
        //    getMapAsync { googleMap ->
        //        // Configure the GoogleMap object here
        //    }
        //}
    }
}