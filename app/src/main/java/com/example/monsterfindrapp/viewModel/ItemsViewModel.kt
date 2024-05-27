package com.example.monsterfindrapp.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.PermissionHandler
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ItemsViewModel(application: Application): AndroidViewModel(application) {
    var showAddItemModal by mutableStateOf(false)

    fun showAddItemModal() {
        showAddItemModal = true
    }

    fun hideAddItemModal() {
        showAddItemModal = false
    }

    private val _monsterItems = MutableStateFlow<List<MonsterItem>>(emptyList())
    //val monsterItems: StateFlow<List<MonsterItem>> = _monsterItems.asStateFlow()


    fun getFilteredItems(query: String): Flow<List<MonsterItem>> {
        return if (query.isEmpty()) {
            _monsterItems.asStateFlow()
        } else {
            _monsterItems.asStateFlow().map { items ->
                items.filter { item ->
                    item.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    init {
        viewModelScope.launch() {
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
                    data?.get("name") as? String ?: "",
                    data?.get("desc") as? String ?: "",
                    data?.get("image") as? String ?: ""
                )
            }
        }
    }

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    fun initializeLaunchers(
        pickImageLauncher: ActivityResultLauncher<Intent>
    ){
        this.pickImageLauncher = pickImageLauncher
    }

    fun checkAndRequestPermission(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ){
        if(PermissionHandler.hasReadExternalStoragePermission(context)){
            launchImagePicker()
            Log.i("CheckAndRequestPermission","Launch Image Picker")
        }else{
            PermissionHandler.requestReadExternalStoragePermission(requestPermissionLauncher)
            Log.i("CheckAndRequestPermission", "Request Permissions")
        }
    }

    fun launchImagePicker(){
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    fun setImageUri(uri:Uri){
        _selectedImageUri.value = uri
    }

    fun uploadImageAndSaveItem(itemName: String, itemDescription: String, imageUri: Uri){
        val storage = FirebaseStorage.getInstance()

        val imageRef: StorageReference = storage.getReference("MonsterStaticImageFolder/$itemName")
        imageRef.putFile(imageUri).addOnSuccessListener {
            taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                uri->
                val imageUrl = uri.toString()
                saveItemToDatabase(itemName, itemDescription, imageUrl)
            }
        }
    }

    private fun saveItemToDatabase(itemName: String, itemDescription: String, imageUrl: String){
        val db = FirebaseFirestore.getInstance()
        val newItem = hashMapOf(
            "name" to itemName,
            "desc" to itemDescription,
            "image" to imageUrl
        )
        db.collection("MonsterItems").document(itemName).set(newItem)
    }
    fun removeItem(item: MonsterItem){
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        val imageRef = storage.getReferenceFromUrl(item.imageUrl)
        imageRef.delete().addOnSuccessListener {
            db.collection("MonsterItems").document(item.name).delete()
        }
    }

}

