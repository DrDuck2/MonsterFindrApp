package com.example.monsterfindrapp.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.monsterfindrapp.IHandleImages
import com.example.monsterfindrapp.utility.LoadingStateManager
import com.example.monsterfindrapp.model.MonsterItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ItemsViewModel(application: Application): AndroidViewModel(application), IHandleImages {

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    override fun setImageUri(uri: Uri){
        _selectedImageUri.value = uri
    }
    override fun removeImageUri(){
        _selectedImageUri.value = null
    }

    fun uploadImageAndSaveItem(itemName: String, itemDescription: String, imageUri: Uri){
        LoadingStateManager.setIsLoading(true)
        val storage = FirebaseStorage.getInstance()

        val imageRef: StorageReference = storage.getReference("MonsterStaticImageFolder/$itemName")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                taskSnapshot ->
                taskSnapshot.storage.downloadUrl
                    .addOnSuccessListener {
                        uri->
                        val imageUrl = uri.toString()
                        saveItemToDatabase(itemName, itemDescription, imageUrl)
            }.addOnFailureListener{ e ->
                        Log.w("UploadImageAndSaveItem", "Error Retrieving Image Url from Storage ${e.message})", e)
                        LoadingStateManager.setErrorMessage(e.message ?: "\"Error Retrieving Image Url From Storage\"")
            }
        }.addOnFailureListener{ e ->
                Log.w("UploadImageAndSaveItem", "Error Adding Image To Storage ${e.message})", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Image To Storage\"")
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
            .addOnSuccessListener {
                Log.i("SaveItemToDatabase", "Successfully Added Item")
                LoadingStateManager.setIsSuccess(true)
            }
            .addOnFailureListener { e->
                Log.w("SaveItemToDatabase", "Error Adding Item to Database ${e.message})", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Adding Item From Database\"")
            }
    }

    fun removeItem(item: MonsterItem){
        LoadingStateManager.setIsLoading(true)
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        val imageRef = storage.getReferenceFromUrl(item.imageUrl)
        imageRef.delete()
            .addOnSuccessListener {
            Log.i("RemoveItem", "Successfully Removed The Image")
            db.collection("MonsterItems").document(item.name).delete()
                .addOnSuccessListener {
                    LoadingStateManager.setIsSuccess(true)
                    Log.i("RemoveItem", "Successfully Removed Item")
                }
                .addOnFailureListener { e->
                    Log.w("RemoveItem", "Error Removing Item ${e.message})", e)
                    LoadingStateManager.setErrorMessage(e.message ?: "\"Error Removing Item From Database\"")
                }
        }.addOnFailureListener { e->
                Log.w("RemoveItem", "Error Removing Image ${e.message})", e)
                LoadingStateManager.setErrorMessage(e.message ?: "\"Error Removing Image From Storage\"")
            }
    }


}

