package com.example.monsterfindrapp.utility

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.monsterfindrapp.IHandleImages
import firebase.com.protolitewrapper.BuildConfig
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date

class PermissionCameraHandler(
    private val context: Context,
    private val viewModel: ViewModel
){
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoPath: String

    fun initializeLaunchers(
        requestPermissionLauncher: ActivityResultLauncher<String>,
        takePictureLauncher: ActivityResultLauncher<Uri>
    ){
        this.requestPermissionLauncher = requestPermissionLauncher
        this.takePictureLauncher = takePictureLauncher
    }

    fun checkAndRequestPermissionCamera(){
        if(PermissionHandler.hasCameraPermission(context)){
            openCamera()
        }else{
            PermissionHandler.requestCameraPermission(requestPermissionLauncher)
        }
    }

    private fun openCamera(){
        val photoFile: File = createImageFile()
        val photoURI = FileProvider.getUriForFile(
            context,
            "com.example.monsterfindrapp.fileprovider",
            photoFile
        )


        takePictureLauncher.launch(photoURI)
    }

    private fun createImageFile(): File{
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        @Composable
        fun rememberCameraPermissionLauncher(
            permissionCameraHandler: PermissionCameraHandler,
            context: Context
        ): ActivityResultLauncher<String> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    permissionCameraHandler.openCamera()
                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        @Composable
        fun rememberTakePictureLauncher(
            permissionCameraHandler: PermissionCameraHandler,
            viewModel: IHandleImages
        ): ActivityResultLauncher<Uri> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { success: Boolean ->
                if (success) {
                    val photoURI = Uri.fromFile(File(permissionCameraHandler.currentPhotoPath))
                    viewModel.setImageUri(photoURI)
                }
            }
        }
    }
}