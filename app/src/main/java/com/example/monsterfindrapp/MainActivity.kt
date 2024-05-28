package com.example.monsterfindrapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.monsterfindrapp.view.AdminDashboardScreen
import com.example.monsterfindrapp.view.HandleNotificationScreen
import com.example.monsterfindrapp.view.ItemsScreen
import com.example.monsterfindrapp.view.LoginRegisterScreen
import com.example.monsterfindrapp.view.MapScreen
import com.example.monsterfindrapp.view.RequestEntryScreen
import com.example.monsterfindrapp.view.RequestsScreen
import com.example.monsterfindrapp.view.UsersScreen
import com.example.monsterfindrapp.viewModel.AdminDashboardViewModel
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel
import com.example.monsterfindrapp.viewModel.ItemsViewModel
import com.example.monsterfindrapp.viewModel.LoginRegisterViewModel
import com.example.monsterfindrapp.viewModel.MapViewModel
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel
import com.example.monsterfindrapp.viewModel.RequestsViewModel
import com.example.monsterfindrapp.viewModel.UsersViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemsViewModel = ViewModelProvider(this)[ItemsViewModel::class.java]
        val requestEntryViewModel = ViewModelProvider(this)[RequestEntryViewModel::class.java]
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "MapScreen"){
                composable("LoginRegisterScreen"){
                    LoginRegisterScreen(navController = navController, LoginRegisterViewModel())
                }
                composable("MapScreen"){
                    MapScreen(navController = navController, MapViewModel())
                }
                composable("RequestEntryScreen"){
                    RequestEntryScreen(navController = navController, requestEntryViewModel)
                }
                composable("HandleNotificationScreen"){
                    HandleNotificationScreen(navController = navController, HandleNotificationViewModel())
                }
                composable("AdminDashboardScreen"){
                    AdminDashboardScreen(navController = navController, AdminDashboardViewModel())
                }
                composable("UsersScreen"){
                    UsersScreen(navController = navController, UsersViewModel())
                }
                composable("ItemsScreen"){
                    ItemsScreen(navController = navController, itemsViewModel)
                }
                composable("RequestsScreen"){
                    RequestsScreen(navController = navController, RequestsViewModel())
                }
            }
        }
    }
}