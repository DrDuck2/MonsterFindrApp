package com.example.monsterfindrapp.utility

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

object PermissionHandler {
    fun hasReadExternalStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                    PermissionChecker.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PermissionChecker.PERMISSION_GRANTED
        }
    }

    fun requestReadExternalStoragePermission(
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermissionLauncher.launch(permission)
    }

    fun hasLocationPermission(context: Context): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PermissionChecker.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PermissionChecker.PERMISSION_GRANTED
        }else{
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PermissionChecker.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PermissionChecker.PERMISSION_GRANTED
        }
    }

    fun requestLocationPermission(
        requestPermissionLauncher: ActivityResultLauncher<String>
    ){
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.ACCESS_FINE_LOCATION
            Manifest.permission.ACCESS_COARSE_LOCATION
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
            Manifest.permission.ACCESS_COARSE_LOCATION
        }
        requestPermissionLauncher.launch(permission)
    }



}