package com.example.soilroverapp.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.soilroverapp.ui.bluetooth.BluetoothFragment
import com.example.soilroverapp.ui.bluetooth.MESSAGE_READ
import com.example.soilroverapp.ui.bluetooth.MESSAGE_WRITE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothService {
    private lateinit var handler: Handler
    private lateinit var bluetoothThread: ConnectedThread
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothFragment: BluetoothFragment

    companion object {
        private val instanse: BluetoothService = BluetoothService()
        fun getInstance(): BluetoothService {
            return instanse
        }
    }

    fun permissionRequester(): ActivityResultLauncher<Array<String>> {
        return bluetoothFragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // returns Map<String, Boolean> where String represents the
            // permission requested and boolean represents the
            // permission granted or not
            // iterate over each entry of map and take action needed for
            // each permission requested

            bluetoothManager =
                bluetoothFragment.requireActivity().applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

            val antall = permissions.size
            var perm = 0
            permissions.forEach { actionMap ->
                if (actionMap.value) {
                    perm++
                    Log.i("DEBUG", actionMap.key + " permission granted")
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
                    Toast.makeText(
                        bluetoothFragment.requireContext(),
                        "Bluetooth er ikke slått på",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    val bil = bluetoothManager.adapter?.bondedDevices?.filter { device ->
                        device.name.equals("HC-05")
                    }?.getOrNull(0)
                    if (bil != null) {
                        Toast.makeText(
                            bluetoothFragment.requireContext(),
                            "hei1",
                            Toast.LENGTH_SHORT
                        ).show()
                        ConnectThread(bil).start()
                    } else {
                        Log.i("DEBUG", "fant ikke bil!")
                        Toast.makeText(
                            bluetoothFragment.requireContext(),
                            "hei2",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // TODO: permissions not given
            }
        }
    }

    fun kobleTilBluetooth(bluetooth: BluetoothFragment) {
        bluetoothFragment = bluetooth
        val bluetoothManager =
            bluetoothFragment.context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.getAdapter() == null) {
            // TODO: Bluetooth not available
            Toast.makeText(
                bluetoothFragment.requireContext(),
                "Bluetooth støttes ikke på enhenten",
                Toast.LENGTH_SHORT
            ).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionRequester().launch(
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            } else {
                permissionRequester().launch(
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                    )
                )
            }
        }
    }

    fun handleArduinoMsg() {
        handler =
            object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MESSAGE_READ -> {
                            val arduinomsg: String = msg.obj.toString();
                        }
                    }
                }
            }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val uuid = device.uuids[0].uuid
            device.createRfcommSocketToServiceRecord(uuid)
        }

        override fun run() {
            bluetoothManager.adapter.cancelDiscovery()
            mmSocket?.let { socket ->
                socket.connect()
                bluetoothThread = ConnectedThread(socket)
                bluetoothThread.start()
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int
            while (true) {
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                val message = String(mmBuffer, 0, numBytes)
                handler.obtainMessage(MESSAGE_READ, message).sendToTarget()
            }
        }

        fun write(input: String) {
            val bytes: ByteArray = input.toByteArray()
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                return
            }
            handler.obtainMessage(MESSAGE_WRITE, -1, -1, mmBuffer).sendToTarget()
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}
