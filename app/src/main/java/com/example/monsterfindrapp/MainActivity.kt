package com.example.monsterfindrapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.example.monsterfindrapp.view.RequestsScreenDetails
import com.example.monsterfindrapp.view.RequestsScreenRequests
import com.example.monsterfindrapp.view.SelectLocationScreen
import com.example.monsterfindrapp.view.UsersScreen
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
        val requestsViewModel = RequestsViewModel()
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "LoginRegisterScreen"){
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
                    AdminDashboardScreen(navController = navController, MapViewModel())
                }
                composable("UsersScreen"){
                    UsersScreen(navController = navController, UsersViewModel())
                }
                composable("ItemsScreen"){
                    ItemsScreen(navController = navController, itemsViewModel)
                }
                composable("RequestsScreen"){
                    RequestsScreen(navController = navController, requestsViewModel)
                }
                composable("SelectLocationScreen"){
                    SelectLocationScreen(navController = navController, requestEntryViewModel)
                }
                composable("RequestsScreenRequests"){
                    RequestsScreenRequests(navController = navController, requestsViewModel)
                }
                composable("RequestsScreenDetails"){
                    RequestsScreenDetails(navController = navController, requestsViewModel)
                }
            }
        }
    }
}