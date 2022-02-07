package com.example.soilroverapp.ui.fjernstyring

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FjernstyringViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is fjernstyring Fragment"
    }
    val text: LiveData<String> = _text
}