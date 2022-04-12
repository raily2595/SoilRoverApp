package com.example.soilroverapp.ui.bluetooth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel : ViewModel() {

    val btMessage: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}