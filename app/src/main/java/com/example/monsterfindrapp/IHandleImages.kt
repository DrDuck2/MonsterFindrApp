package com.example.monsterfindrapp

import android.net.Uri

interface IHandleImages {
    fun setImageUri(uri: Uri)
    fun removeImageUri()
}