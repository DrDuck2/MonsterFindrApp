package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.model.SideMenuItem

@Composable
fun SideMenuDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    navController: NavController,
    menuItems: List<SideMenuItem>
){
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(200.dp)
    ) {
        menuItems.forEach { menuItem ->
            DropdownMenuItem(onClick = {
                menuItem.action(navController)
                onDismissRequest()
            }, text = {
                Text(menuItem.text)
            })
        }
    }
}


