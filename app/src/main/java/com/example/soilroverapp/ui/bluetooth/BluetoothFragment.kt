package com.example.soilroverapp.ui.bluetooth

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.soilroverapp.R
import com.example.soilroverapp.databinding.FragmentBluetoothBinding
import java.util.jar.Manifest

class BluetoothFragment : Fragment() {

    private var _binding: FragmentBluetoothBinding? = null
    private val PERMISSION_REQUEST_BLUETOOTH_CODE = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bluetoothViewModel =
            ViewModelProvider(this).get(BluetoothViewModel::class.java)

        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textBluetooth
        bluetoothViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        initializeBluetoothOrRequestPermission()

        return root
    }

    private fun initializeBluetoothOrRequestPermission() {
        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN)
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            initializeBluetooth()
        } else {
            requestPermissions(missingPermissions.toTypedArray(), PERMISSION_REQUEST_BLUETOOTH_CODE)
        }
    }

    private fun initializeBluetooth() { ... }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_BLUETOOTH_CODE -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    // all permissions are granted
                    initializeBluetooth()
                } else {
                    // some permissions are not granted
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}