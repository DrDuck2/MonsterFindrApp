package com.example.monsterfindrapp.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.monsterfindrapp.PermissionHandler
import com.example.monsterfindrapp.model.MonsterItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RequestEntryViewModel(application: Application): AndroidViewModel(application) {
    private val _monsterItems = MutableStateFlow<List<MonsterItem>>(emptyList())
    val monsterItems: StateFlow<List<MonsterItem>> = _monsterItems.asStateFlow()

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    fun initializeLaunchers(
        pickImageLauncher: ActivityResultLauncher<Intent>
    ){
        this.pickImageLauncher = pickImageLauncher
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

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri
    fun setImageUri(uri: Uri){
        _selectedImageUri.value = uri
    }
}