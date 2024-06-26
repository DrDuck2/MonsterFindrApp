package com.example.monsterfindrapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.utility.PermissionLocationHandler
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
import com.example.monsterfindrapp.view.StoreItemsScreen
import com.example.monsterfindrapp.view.StoresScreen
import com.example.monsterfindrapp.viewModel.StoreItemsViewModel
import com.example.monsterfindrapp.view.UsersScreen
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel
import com.example.monsterfindrapp.viewModel.ItemsViewModel
import com.example.monsterfindrapp.viewModel.LoginRegisterViewModel
import com.example.monsterfindrapp.viewModel.MapViewModel
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel
import com.example.monsterfindrapp.viewModel.RequestsViewModel
import com.example.monsterfindrapp.viewModel.UserStatusViewModel
import com.example.monsterfindrapp.viewModel.UsersViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemsViewModel = ViewModelProvider(this)[ItemsViewModel::class.java]
        val requestEntryViewModel = ViewModelProvider(this)[RequestEntryViewModel::class.java]
        val requestsViewModel = RequestsViewModel()
        val permissionLocationHandler = PermissionLocationHandler(this, requestEntryViewModel)
        val storeItemsViewModel = StoreItemsViewModel()

        val userStatusViewModel = ViewModelProvider(this)[UserStatusViewModel::class.java]
        userStatusViewModel.checkUserStatus()

        setContent {
            val navController = rememberNavController()

            val isUserSuspended by userStatusViewModel.isUserSuspended.observeAsState()
            val isUserBanned by userStatusViewModel.isUserBanned.observeAsState()
            LaunchedEffect(isUserSuspended, isUserBanned){
                if(isUserSuspended == true || isUserBanned == true){
                    AuthenticationManager.logout()
                    navController.navigate("LoginRegisterScreen"){
                        popUpTo("LoginRegisterScreen") {inclusive = true}
                    }
                }
            }


            NavHost(navController = navController, startDestination = "LoginRegisterScreen"){
                composable("LoginRegisterScreen"){
                    LoginRegisterScreen(navController = navController, LoginRegisterViewModel())
                }
                composable("MapScreen"){
                    MapScreen(navController = navController, MapViewModel())
                }
                composable("RequestEntryScreen"){
                    DisposableEffect(Unit) {
                        onDispose {
                            requestEntryViewModel.clearState()
                            permissionLocationHandler.clearState()
                        }
                    }
                    RequestEntryScreen(navController = navController, requestEntryViewModel, permissionLocationHandler)
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
                    SelectLocationScreen(navController = navController, permissionLocationHandler)
                }
                composable("RequestsScreenRequests"){
                    RequestsScreenRequests(navController = navController, requestsViewModel)
                }
                composable("RequestsScreenDetails"){
                    RequestsScreenDetails(navController = navController, requestsViewModel)
                }
                composable("StoresScreen"){
                    StoresScreen(navController = navController, storeItemsViewModel, permissionLocationHandler)
                }
                composable("StoreItemsScreen"){
                    StoreItemsScreen(navController = navController, storeItemsViewModel)
                }
            }
        }
    }
}