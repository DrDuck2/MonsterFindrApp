package com.example.monsterfindrapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.viewModel.RequestEntryViewModel

class PermissionImageHandler(
    private val context: Context,
    private val viewModel: ViewModel
) {
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun initializeLaunchers(
        pickImageLauncher: ActivityResultLauncher<Intent>,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ){
        this.pickImageLauncher = pickImageLauncher
        this.requestPermissionLauncher = requestPermissionLauncher
    }

    fun checkAndRequestPermissionImages(){
        if(PermissionHandler.hasReadExternalStoragePermission(context)){
            launchImagePicker()
        }else{
            PermissionHandler.requestReadExternalStoragePermission(requestPermissionLauncher)
        }
    }

    fun launchImagePicker(){
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    fun setImageUri(uri: Uri){
        _selectedImageUri.value = uri
    }
    fun removeImageUri(){
        _selectedImageUri.value = null
    }

    companion object{
        @Composable
        private fun rememberPermissionLauncher(
            requestEntryViewModel: RequestEntryViewModel,
            context: Context
        ): ActivityResultLauncher<String> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    requestEntryViewModel.launchImagePicker()
                } else {
                    // Handle permission denial
                }
            }
        }

        @Composable
        private fun rememberPickImageLauncher(
            requestEntryViewModel: RequestEntryViewModel
        ): ActivityResultLauncher<Intent> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        requestEntryViewModel.setImageUri(uri)
                    }
                }
            }
        }
    }

}