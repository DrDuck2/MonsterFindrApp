package com.example.monsterfindrapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel

@Composable
fun RequestEntryScreen(navController: NavController, viewModel: RequestEntryViewModel){
    var location by remember { mutableStateOf("") }
    var drinkType by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var pictureUri by remember{mutableStateOf("")}

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center){

        TextField(
            value = location,
            onValueChange = {
                location = it },
            label = {Text("Location")}
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = drinkType,
            onValueChange = {drinkType = it},
            label = {Text("Drink Type")}
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = availability,
            onValueChange = {availability = it},
            label = {Text("Availability")}
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = price,
            onValueChange = {price = it},
            label = {Text("Price")}
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {

        }){
            Text("Select Picture")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.popBackStack()
        }){
            Text("Submit Entry")
        }
    }
}