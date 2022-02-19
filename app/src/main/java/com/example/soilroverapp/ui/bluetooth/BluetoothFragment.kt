package com.example.soilroverapp.ui.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.R
import com.example.soilroverapp.databinding.FragmentBluetoothBinding


class BluetoothFragment : Fragment() {

    val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var _binding: FragmentBluetoothBinding? = null
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // returns Map<String, Boolean> where String represents the
            // permission requested and boolean represents the
            // permission granted or not
            // iterate over each entry of map and take action needed for
            // each permission requested

            val antall = permissions.size
            var perm = 0
            permissions.forEach { actionMap ->
                if (actionMap.value) {
                    perm = +1
                    Log.i("DEBUG", "permission granted")
                } else {
                    // if permission denied then check whether never
                    // ask again is selected or not by making use of
                    // !ActivityCompat.shouldShowRequest
                    // PermissionRationale(requireActivity(),
                    // Manifest.permission.CAMERA)
                    Log.i("DEBUG", "permission denied")
                }
            }
            if (perm == antall) {
                if (bluetoothManager.adapter?.isEnabled == false) {
                    // TODO: Bluetooth not enabled
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    //registerForActivityResult()
                    //Toast.makeText(applicationContext,"Bluetooth er ikke slått på",Toast.LENGTH_SHORT).show();
                } else {
                    val bil  = bluetoothManager.adapter?.bondedDevices?.filter { device -> device.name.equals("SoilRover")}?.get(0)
                }
            } else {
                // TODO: permissions not given
            }
        }

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
        val bluetoothButton: Button = requireView().findViewById(R.id.bluetoothbutton)

        bluetoothButton.setOnClickListener {

            val bluetoothManager =
                context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager.getAdapter() == null) {
                // TODO: Bluetooth not available
                //Toast.makeText(applicationContext,"Bluetooth støttes ikke på enhenten",Toast.LENGTH_SHORT).show();
            } else {
                requestPermission.launch(
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                    )
                )
            }
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}