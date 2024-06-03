package com.example.monsterfindrapp.utility

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LoadingStateManager {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    private val _smallLoading = MutableStateFlow(false)
    val smallLoading: StateFlow<Boolean> = _smallLoading
    private val _smallSuccess = MutableStateFlow(false)
    val smallSuccess: StateFlow<Boolean> = _smallSuccess
    private val _smallFailure = MutableStateFlow(false)
    val smallFailure: StateFlow<Boolean> = _smallFailure
    private val _smallErrorMessage = MutableStateFlow<String?>(null)
    val smallErrorMessage: StateFlow<String?> = _smallErrorMessage

    fun setSmallLoading(value: Boolean){
        _smallLoading.value = value
    }
    fun setSmallSuccess(value: Boolean){
        _smallSuccess.value = value
    }
    fun setSmallErrorMessage(value: String?){
        _smallErrorMessage.value = value
    }
    fun setSmallFailure(value: Boolean){
        _smallFailure.value = value
    }

    fun isLoading(): Boolean{
        return isLoading.value
    }
    fun isSuccess(): Boolean{
        return isSuccess.value
    }
    fun errorMessage(): String?{
        return errorMessage.value
    }

    fun setIsLoading(value: Boolean) {
        _isLoading.value = value
    }

    fun setIsSuccess(value: Boolean){
        _isSuccess.value = value
    }

    fun setErrorMessage(value: String?){
        _errorMessage.value = value
    }

    fun resetLoading() {
        _isLoading.value = false
        _isSuccess.value = false
        _errorMessage.value = null
        _smallLoading.value = false
        _smallSuccess.value = false
        _smallErrorMessage.value = null
        _smallFailure.value = false
    }
}