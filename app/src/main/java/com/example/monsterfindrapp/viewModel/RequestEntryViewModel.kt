package com.example.monsterfindrapp.viewModel

import android.app.Application
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.IHandleImages
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RequestEntryViewModel(application: Application) : AndroidViewModel(application), IHandleImages {

    private val _selectedDrink = MutableStateFlow<MonsterItem?>(null)
    val selectedDrink: StateFlow<MonsterItem?> = _selectedDrink


    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    override fun setImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }

    override fun removeImageUri() {
        _selectedImageUri.value = null
    }

    fun clearState() {
        _selectedDrink.value = null
        removeImageUri()
    }

    fun selectDrink(drink: MonsterItem) {
        _selectedDrink.value = drink
    }


    fun callSubmitEntry(
        storeLocation: Locations,
        item: MonsterItem,
        availability: String,
        price: String,
        proofImage: Uri
    ) {
        LoadingStateManager.setIsLoading(true)
        viewModelScope.launch {
            submitEntry(storeLocation, item, availability, price, proofImage)
        }
    }

    fun callSubmitEntryCurrentLocation(
        currentLocation: Location,
        item: MonsterItem,
        availability: String,
        price: String,
        proofImage: Uri
    ) {
        LoadingStateManager.setIsLoading(true)
        viewModelScope.launch {
            submitEntryCurrentLocation(currentLocation, item, availability, price, proofImage)
        }
    }

    private fun submitEntry(
        storeLocation: Locations,
        item: MonsterItem,
        availability: String,
        price: String,
        proofImage: Uri
    ) {
        val storageRef = Firebase.storage.reference.child("RequestEntryImages/${UUID.randomUUID()}")

        storageRef.putFile(proofImage)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        val db = FirebaseFirestore.getInstance()
                        val currentUserId = AuthenticationManager.getCurrentUserId()

                        if (currentUserId != null) {
                            Log.i("SubmitEntry", currentUserId)

                            val entryData = hashMapOf(
                                "item" to item.id,
                                "availability" to availability,
                                "price" to price.toDouble(),
                                "coordinates" to storeLocation.location,
                                "proof_image" to uri.toString(),
                                "createdAt" to Timestamp.now()
                            )

                            val userDocRef = db.collection("RequestEntries").document(currentUserId)
                            userDocRef.set(emptyMap<String, Any>()) // Create if it doesn't exist
                                .addOnSuccessListener {
                                    val requestsCollectionRef = userDocRef.collection("Requests")

                                    val storeLocationDocRef =
                                        requestsCollectionRef.document(storeLocation.name)

                                    storeLocationDocRef.set(entryData)
                                        .addOnSuccessListener {
                                            Log.i("RequestEntry", "Entry submitted successfully")
                                            LoadingStateManager.setIsSuccess(true)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("RequestEntry", "Error submitting entry: $e", e)
                                            LoadingStateManager.setErrorMessage(
                                                e.message ?: "Error Submitting Entry"
                                            )
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("RequestEntry", "Error creating user document: $e", e)
                                    LoadingStateManager.setErrorMessage(
                                        e.message ?: "Error Submitting Entry"
                                    )
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("RequestEntry", "Error uploading proof image: $e", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "Error Submitting Entry")
                    }
            }
    }

    private fun submitEntryCurrentLocation(
        currentLocation: Location,
        item: MonsterItem,
        availability: String,
        price: String,
        proofImage: Uri
    ) {


        val storageRef = Firebase.storage.reference.child("RequestEntryImages/${UUID.randomUUID()}")

        storageRef.putFile(proofImage)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        val db = FirebaseFirestore.getInstance()
                        val currentUserId = AuthenticationManager.getCurrentUserId()

                        if (currentUserId != null) {
                            Log.i("SubmitEntry", currentUserId)

                            val entryData = hashMapOf(
                                "item" to item.id,
                                "availability" to availability,
                                "price" to price.toDouble(),
                                "coordinates" to GeoPoint(
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                ),
                                "proof_image" to uri.toString(),
                                "createdAt" to Timestamp.now()
                            )

                            val userDocRef = db.collection("RequestEntries").document(currentUserId)
                            userDocRef.set(emptyMap<String, Any>()) // Create if it doesn't exist
                                .addOnSuccessListener {
                                    val requestsCollectionRef = userDocRef.collection("Requests")

                                    val storeLocationDocRef =
                                        requestsCollectionRef.document("NewLocation - ${UUID.randomUUID()}")

                                    storeLocationDocRef.set(entryData)
                                        .addOnSuccessListener {
                                            Log.i("RequestEntry", "Entry submitted successfully")
                                            LoadingStateManager.setIsSuccess(true)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("RequestEntry", "Error submitting entry: $e", e)
                                            LoadingStateManager.setErrorMessage(
                                                e.message ?: "Error Submitting Entry"
                                            )
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("RequestEntry", "Error creating user document: $e", e)
                                    LoadingStateManager.setErrorMessage(
                                        e.message ?: "Error Submitting Entry"
                                    )
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("RequestEntry", "Error uploading proof image: $e", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "Error Submitting Entry")
                    }
            }
    }

}
