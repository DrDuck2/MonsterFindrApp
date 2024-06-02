package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.model.SideMenuItem


@Composable
fun ScreenHeader(
    showSideMenu: Boolean,
    onMenuClick: () -> Unit,
    navController: NavController,
    menuItems: List<SideMenuItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }


    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    onSearchQueryChange(it)
                    showDropdownMenu = true
                },
                label = { Text("Search") },
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
    }
}