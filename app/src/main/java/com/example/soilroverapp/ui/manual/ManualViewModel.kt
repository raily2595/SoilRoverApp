package com.example.soilroverapp.ui.manual

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Modell som holder på data for manualfragmentet
 *
 * Holder på navnet for fil som er lastet inn i PDF-fremviseren
 */
class ManualViewModel : ViewModel() {
    val pdfFile: MutableLiveData<String> by lazy {
        MutableLiveData<String>("PM utstilling 2022.pdf")
    }
}