package com.example.monsterfindrapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.monsterfindrapp.utility.LoadingStateManager

@Composable
fun SmallLoadingIconOverlay(
){
    val isLoading by LoadingStateManager.smallLoading.collectAsState()
    val isSuccess by LoadingStateManager.smallSuccess.collectAsState()
    val errorMessage by LoadingStateManager.smallErrorMessage.collectAsState()


    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = Color.Black
        )
    }
    if (isSuccess) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "Current Location",
            modifier = Modifier.size(24.dp),
            tint = Color.Green
        )
    }
    if (errorMessage != null) {
        Text(
            text = "Error: $errorMessage",
            fontSize = 16.sp,
            color = Color.Red
        )
    }
}


@Composable
fun NavigateLoadingOverlay(
    onNavigate: () -> Unit,
    setAlpha: Float
) {
    val isSuccess by LoadingStateManager.isLoading.collectAsState()
    val errorMessage by LoadingStateManager.errorMessage.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
        color = Color.Black.copy(alpha = setAlpha),

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if(errorMessage != null){
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ){
                    Text(text = "Error: $errorMessage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
                            }
                        })
                }
                LoadingStateManager.setIsLoading(false)
                onNavigate()
            }else if(isSuccess){
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ){
                    Text(text = "Entry Submitted",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
                            }
                        })
                }
                LoadingStateManager.setIsLoading(false)
                onNavigate()
            }else{
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun LoadingOverlay(
    onDismiss: () -> Unit,
) {
    val isSuccess by LoadingStateManager.isSuccess.collectAsState()
    val errorMessage by LoadingStateManager.errorMessage.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .clickable(interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (errorMessage != null || isSuccess) {
                        onDismiss()
                    }
                }),
        color = Color.Black.copy(alpha = 0.5f),

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ) {
                    androidx.compose.material3.Text(text = "Error: $errorMessage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
                            }
                        })
                }
            } else if (isSuccess) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ) {
                    androidx.compose.material3.Text(text = "Success",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
                            }
                        })
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White
                )
            }
        }
    }
}