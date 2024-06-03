package com.example.monsterfindrapp.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.IHandleImages

class PermissionImageHandler(
    private val context: Context,
    private val viewModel: ViewModel
) {
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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

    companion object {
        @Composable
        fun rememberPermissionLauncher(
            permissionImageHandler: PermissionImageHandler,
            context: Context
        ): ActivityResultLauncher<String> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    permissionImageHandler.launchImagePicker()
                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @Composable
        fun rememberPickImageLauncher(
            permissionImageHandler: PermissionImageHandler,
            viewModel: IHandleImages
        ): ActivityResultLauncher<Intent> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        viewModel.setImageUri(uri)
                    }
                }
            }
        }
    }

}