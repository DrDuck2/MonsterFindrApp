package com.example.monsterfindrapp.view

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.monsterfindrapp.viewModel.LoginRegisterViewModel
import androidx.compose.ui.window.Dialog
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.model.LoginState
import com.example.monsterfindrapp.model.RegisterState

@Composable
fun LoginRegisterScreen(navController: NavController, viewModel: LoginRegisterViewModel) {
    // If user is Admin Go to AdminDashboardScreen otherwise go to MapScreen
    if(AuthenticationManager.isUserAuthenticated()){
        AuthenticationManager.checkUserIsAdmin { isAdmin ->
            if(isAdmin) navController.navigate("AdminDashboardScreen") else navController.navigate("MapScreen")}
    // If user not logged in
    }else{
        // On Back press close the app
        val activity = (LocalContext.current as? Activity)
        BackHandler {
            activity?.finish()
        }
    }

    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    var showError by remember { mutableStateOf(false) }
    val passwordVisible = remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = loginEmail,
            onValueChange = { loginEmail = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = loginPassword,
            onValueChange = { loginPassword = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if(passwordVisible.value)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = {
                    passwordVisible.value = !passwordVisible.value
                }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible.value) "Hide password" else "Show password")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (showError) {
            Text(text = "Please fill in all fields", color = Color.Red)
        }
        Button(onClick = {
            if(loginPassword.isEmpty() || loginEmail.isEmpty()){
                showError = true
            }else{
                showError = false
                viewModel.login(loginEmail, loginPassword)
            }
        }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Not a user? Register!",
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { viewModel.showRegisterModal() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        when(loginState){
            is LoginState.Loading -> {
                Text("Loading...")
            }
            is LoginState.Error -> {
                Text("Error: ${(loginState as LoginState.Error).message}")
            }
            is LoginState.Success ->{
                AuthenticationManager.checkUserIsAdmin { isAdmin ->
                    if(isAdmin){
                        navController.navigate("AdminDashboardScreen")
                        Log.d("Admin","Logged In user is admin")
                    }
                    else{
                        navController.navigate("MapScreen")
                        Log.d("Admin","Logged In user is not admin")

                    }
                }
            }
            else -> { /* Do Nothing */}
        }
    }

    if(viewModel.showRegisterModal){
        RegisterModal(
            onDismiss = {viewModel.hideRegisterModal()},
            onRegister = {registerEmail, registerPassword, repeatPassword ->
                viewModel.register(registerEmail, registerPassword, repeatPassword)
            },
            viewModel
        )
    }
}

@Composable
fun RegisterModal(
    onDismiss: () -> Unit,
    onRegister: (String, String, String) -> Unit,
    viewModel: LoginRegisterViewModel
){
    var registerEmail by remember {mutableStateOf("")}
    var registerPassword by remember {mutableStateOf("")}
    var repeatPassword by remember {mutableStateOf("")}

    val registerState by viewModel.registerState.collectAsState()

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember {mutableStateOf("")}

    val passwordVisible = remember { mutableStateOf(false) }
    val repeatPasswordVisible = remember {mutableStateOf(false)}

    Dialog(onDismissRequest = onDismiss){
        Surface(color = MaterialTheme.colorScheme.background){
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text("Register", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = registerEmail,
                    onValueChange = {registerEmail = it},
                    label = {Text("Email")}
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = registerPassword,
                    onValueChange = {registerPassword = it},
                    label = {Text("Password")},
                    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if(passwordVisible.value)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = {
                            passwordVisible.value = !passwordVisible.value
                        }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible.value) "Hide password" else "Show password")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = {repeatPassword = it},
                    label = {Text("Repeat Password")},
                    visualTransformation = if (repeatPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if(repeatPasswordVisible.value)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = {
                            repeatPasswordVisible.value = !repeatPasswordVisible.value
                        }) {
                            Icon(imageVector = image, contentDescription = if (repeatPasswordVisible.value) "Hide password" else "Show password")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (showError) {
                    Text(text = errorMessage, color = Color.Red)
                }
                Button(onClick = {
                    if(registerEmail.isEmpty() || registerPassword.isEmpty() || repeatPassword.isEmpty()){
                        errorMessage = "Please fill in all fields"
                        showError = true
                    }else if(registerPassword != repeatPassword){
                        errorMessage = "Passwords do not match"
                        showError = true
                    }else{
                        showError = false
                        onRegister(registerEmail, registerPassword, repeatPassword)
                    }
                }){
                    Text("Register")
                }
                when(registerState){
                    is RegisterState.Loading -> {
                        Text("Registering...")
                    }
                    is RegisterState.Error -> {
                        Text("Error: ${(registerState as RegisterState.Error).message}")
                    }
                    is RegisterState.Success ->{
                        viewModel.hideRegisterModal()
                    }
                    else -> { /* Do Nothing */ }
                }
            }
        }
    }
}

