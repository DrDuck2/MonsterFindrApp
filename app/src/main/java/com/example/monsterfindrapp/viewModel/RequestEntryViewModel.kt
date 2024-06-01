package com.example.monsterfindrapp.viewModel

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.AuthenticationManager
import com.example.monsterfindrapp.PermissionHandler
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.StoreItem
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.type.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RequestEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val _monsterItems = MutableStateFlow<List<MonsterItem>>(emptyList())
    val monsterItems: StateFlow<List<MonsterItem>> = _monsterItems.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    fun initializeLaunchers(pickImageLauncher: ActivityResultLauncher<Intent>) {
        this.pickImageLauncher = pickImageLauncher
    }

    init {
        viewModelScope.launch {
            getMonsterItems().collect { items ->
                _monsterItems.value = items
            }
        }
    }

    private fun getMonsterItems(): Flow<List<MonsterItem>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("MonsterItems").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val data = document.data
                MonsterItem(
                    document.id,
                    data?.get("name") as? String ?: "",
                    data?.get("desc") as? String ?: "",
                    data?.get("image") as? String ?: ""
                )
            }
        }
    }

    fun checkAndRequestPermissionImages(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        if (PermissionHandler.hasReadExternalStoragePermission(context)) {
            launchImagePicker()
        } else {
            PermissionHandler.requestReadExternalStoragePermission(requestPermissionLauncher)
        }
    }

    fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun setImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun removeImageUri(){
        _selectedImageUri.value = null
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PermissionChecker.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
                    PermissionChecker.PERMISSION_GRANTED

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            if (isLocationEnabled) {
                fusedLocationProviderClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    _location.value = location
                    _selectedLocation.value = null
                    setLocationText("Current Location")
                    Log.i("Location", "${location.latitude} ${location.longitude}")
                }
            }else{
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

    private val _locationText = MutableStateFlow<String>("")
    val locationText: StateFlow<String> = _locationText.asStateFlow()

    fun setLocationText(text: String){
        _locationText.value = text
    }


    private val _locations = MutableStateFlow<List<Locations>>(emptyList())
    val locations: StateFlow<List<Locations>> = _locations.asStateFlow()

    private val _selectedLocation = MutableStateFlow<Locations?>(null)
    val selectedLocation: StateFlow<Locations?> = _selectedLocation

    private val _selectedDrink = MutableStateFlow<MonsterItem?>(null)
    val selectedDrink: StateFlow<MonsterItem?> = _selectedDrink

    fun selectDrink(drink: MonsterItem) {
        _selectedDrink.value = drink
    }

    fun selectStoreLocation(storeLocation: Locations) {
        _selectedLocation.value = storeLocation
        _location.value = null
    }

    init {
        viewModelScope.launch {
            getLocations().collect { locations ->
                _locations.value = locations
            }
        }
    }
    private fun getLocations(): Flow<List<Locations>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("Locations").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val name = document.id
                val location = document.getGeoPoint("coordinates")!!
                val items = getItemsForLocation(db, name)
                Locations(
                    name = name,
                    location = location,
                    items = items
                )
            }
        }
    }
    private suspend fun getItemsForLocation(db: FirebaseFirestore, locationName: String): List<StoreItem>{
        val itemsSnapshot = db.collection("Locations").document(locationName).collection("Items").get().await()
        return itemsSnapshot.documents.map {document->
            val price = document.getDouble("price")!!
            val availability = document.getString("availability")!!
            val lastUpdated = document.getTimestamp("last_updated")!!.toDate()
            val monsterItemId = document.id
            val monsterItem = getMonsterItem(db, monsterItemId)
            StoreItem(
                price = price,
                availability = availability,
                lastUpdated = lastUpdated,
                monsterItem = monsterItem
            )
        }
    }
    private suspend fun getMonsterItem(db: FirebaseFirestore, monsterItemId: String): MonsterItem {
        val monsterItemSnapshot = db.collection("MonsterItems").document(monsterItemId).get().await()
        val id = monsterItemSnapshot.id
        val name = monsterItemSnapshot.getString("name")!!
        val description = monsterItemSnapshot.getString("desc")!!
        val imageUrl = monsterItemSnapshot.getString("image")!!
        return MonsterItem(
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl
        )
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun setLoading(value: Boolean){
        _isLoading.value = value
    }

    fun submitEntry(storeLocation: Locations, item: MonsterItem, availability: String, price: String, proofImage: Uri ){

        _isLoading.value = true

        val storageRef = Firebase.storage.reference.child("RequestEntryImages/${UUID.randomUUID()}")

        storageRef.putFile(proofImage)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri->
                    val db = Firebase.firestore

                    val entryData = hashMapOf(
                        "item" to item.id,
                        "availability" to availability,
                        "price" to price.toDouble(),
                        "coordinates" to storeLocation.location,
                        "proof_image" to uri.toString(),
                        "created_at" to Timestamp.now()
                    )

                    AuthenticationManager.getCurrentUserId()?.let {
                        db.collection("RequestEntries")
                            .document(it)
                            .collection("Requests")
                            .document(storeLocation.name)
                            .set(entryData)
                            .addOnSuccessListener {
                                Log.i("RequestEntry", "Entry submitted successfully")
                                _isSuccess.value = true
                            }
                            .addOnFailureListener { e ->
                                Log.i("RequestEntry", "Error submitting entry: $e")
                                _errorMessage.value = e.message ?: "Error Submitting Entry"
                            }
                    }
                }
                    .addOnFailureListener { e ->
                        Log.i("RequestEntry", "Error uploading proof image: $e")
                        _errorMessage.value = e.message ?: "Error uploading image"
                    }
            }
    }

    fun submitEntryCurrentLocation(currentLocation: Location, item: MonsterItem, availability: String, price: String, proofImage: Uri){
        _isLoading.value = true

        val storageRef = Firebase.storage.reference.child("RequestEntryImages/${UUID.randomUUID()}")

        storageRef.putFile(proofImage)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri->
                    val db = Firebase.firestore

                    val entryData = hashMapOf(
                        "item" to item.id,
                        "availability" to availability,
                        "price" to price.toDouble(),
                        "coordinates" to GeoPoint(currentLocation.latitude, currentLocation.longitude),
                        "proof_image" to uri.toString(),
                        "created_at" to Timestamp.now()
                    )

                    AuthenticationManager.getCurrentUserId()?.let {
                        db.collection("RequestEntries")
                            .document(it)
                            .collection("Requests")
                            .document("NewLocation - ${UUID.randomUUID()}")
                            .set(entryData)
                            .addOnSuccessListener {
                                Log.i("RequestEntry", "Entry submitted successfully")
                                _isSuccess.value = true
                            }
                            .addOnFailureListener { e ->
                                Log.w("RequestEntry", "Error submitting entry: $e")
                                _errorMessage.value = e.message ?: "\"Error Adding Entry\""
                            }
                    }
                }
                    .addOnFailureListener { e ->
                        Log.w("RequestEntry", "Error uploading proof image: $e")
                        _errorMessage.value = e.message ?: "\"Error Adding Entry\""
                    }
            }
    }
}
