package com.example.monsterfindrapp.utility

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.model.Locations
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionLocationHandler (
    private val context: Context,
    private val viewModel: ViewModel
) {
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()
    private val _locationText = MutableStateFlow("")
    val locationText: StateFlow<String> = _locationText.asStateFlow()
    private val _selectedLocation = MutableStateFlow<Locations?>(null)
    val selectedLocation: StateFlow<Locations?> = _selectedLocation

    fun setLocationText(text: String){
        _locationText.value = text
    }


    fun selectStoreLocation(storeLocation: Locations) {
        _selectedLocation.value = storeLocation
        _location.value = null
    }

    fun clearState(){
        _location.value = null
        _locationText.value = ""
        _selectedLocation.value = null
        LoadingStateManager.resetLoading()
    }

    fun checkAndRequestLocationPermission(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        if (PermissionHandler.hasLocationPermission(context)) {
            getCurrentLocation(context)
        } else {
            PermissionHandler.requestLocationPermission(requestPermissionLauncher)
        }
    }

    fun getCurrentLocation(context: Context) {
        LoadingStateManager.resetLoading()
        LoadingStateManager.setSmallLoading(true)
        LoadingStateManager.setSmallFailure(false)
        LoadingStateManager.setSmallSuccess(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PermissionChecker.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            if (isLocationEnabled) {
                fusedLocationProviderClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    LoadingStateManager.setSmallLoading(false)
                    LoadingStateManager.setSmallSuccess(true)
                    _location.value = location
                    _selectedLocation.value = null
                    setLocationText("Current Location")
                    if(location == null){
                        LoadingStateManager.setSmallFailure(true)
                    }else{
                        Log.i("Location", "${location.latitude} ${location.longitude}")
                    }
                }.addOnFailureListener{ e ->
                    Log.e("GetCurrentLocation", "Error Fetching Current Location: ${e.message}", e)
                    LoadingStateManager.setSmallErrorMessage( e.message ?: "\"Error Fetching Current Location \"")
                }
            }else{
                LoadingStateManager.setSmallLoading(false)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Location is disabled")
                    .setMessage("Please turn on location to use this feature.")
                    .setPositiveButton("Turn on") { _, _ ->
                        // Open the location settings page
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(context , intent, null)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
            }
        }
    }


    companion object {
        @Composable
        fun rememberLocationPermissionLauncher(
            permissionLocationHandler: PermissionLocationHandler,
            context: Context
        ): ActivityResultLauncher<String> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    permissionLocationHandler.getCurrentLocation(context)
                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

