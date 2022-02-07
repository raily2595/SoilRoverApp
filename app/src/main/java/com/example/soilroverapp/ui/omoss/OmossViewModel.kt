package com.example.soilroverapp.ui.omoss

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OmossViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is omoss Fragment"
    }
    val text: LiveData<String> = _text
}