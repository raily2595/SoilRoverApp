package com.example.soilroverapp.ui.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.R
import com.example.soilroverapp.databinding.FragmentBluetoothBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1

class BluetoothFragment : Fragment() {

    private lateinit var handler: Handler
    private lateinit var bluetoothThread: ConnectedThread
    private lateinit var bluetoothManager: BluetoothManager
    private var _binding: FragmentBluetoothBinding? = null
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // returns Map<String, Boolean> where String represents the
            // permission requested and boolean represents the
            // permission granted or not
            // iterate over each entry of map and take action needed for
            // each permission requested

            bluetoothManager =
                requireActivity().applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

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
                        requireContext(),
                        "Bluetooth er ikke slått på",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    val bil = bluetoothManager.adapter?.bondedDevices?.filter { device ->
                        device.name.equals("HC-05")
                    }?.getOrNull(0)
                    if (bil != null) {
                        Toast.makeText(requireContext(), "hei1", Toast.LENGTH_SHORT).show()
                        ConnectThread(bil).start()
                    } else {
                        Log.i("DEBUG", "fant ikke bil!")
                        Toast.makeText(requireContext(), "hei2", Toast.LENGTH_SHORT).show()
                    }
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
            ViewModelProvider(this)[BluetoothViewModel::class.java]

        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textBluetooth
        val nameObserver = Observer<String> { newBtMessage ->
            textView.text = newBtMessage
        }
        bluetoothViewModel.btMessage.observe(viewLifecycleOwner, nameObserver)
        bluetoothViewModel.btMessage.value = "koble til bt"

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_READ -> {
                        val arduinomsg : String = msg.obj.toString();
                        bluetoothViewModel.btMessage.value = arduinomsg;
                    }
                }
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bluetoothButton: Button = requireView().findViewById(R.id.bluetoothbutton)

        bluetoothButton.setOnClickListener {

            val bluetoothManager =
                context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager.getAdapter() == null) {
                // TODO: Bluetooth not available
                Toast.makeText(
                    requireContext(),
                    "Bluetooth støttes ikke på enhenten",
                    Toast.LENGTH_SHORT
                ).show();
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

        val butt:Button = requireView().findViewById((R.id.button))

        butt.setOnClickListener {
            bluetoothThread.write("I clicked the butt")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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