package com.example.soilroverapp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.soilroverapp.ui.startside.StartsideFragment
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1

class BluetoothService {

    /**
     * Tråden som håndterer kommunikasjonen i bakgrunnen.
     */
    lateinit var bluetoothThread: ConnectedThread

    /**
     * Handler som holder på og gjør meldinger tilgjengelig.
     */
    lateinit var bluetoothHandler: Handler

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var startsideFragment: StartsideFragment

    /**
     * Bluetooth Service Singleton
     *
     * Singletonobjekt som sørger for å holde kommunikasjon med kjøretøyet uavhengig av hvor i appen brukeren er.
     */
    companion object {
        private val instanse: BluetoothService = BluetoothService()
        fun getInstance(): BluetoothService {
            return instanse
        }
    }

    /**
     * Kobler til Bluetooth
     *
     * Skaffer tillatelser, finner kjøretøy og setter opp toveiskommunikasjon.
     * Dersom noe går galt vises relevant feilmelding til brukeren.
     */
    // requst permissions
    fun kobleTilBluetooth(bluetooth: StartsideFragment) {
        startsideFragment = bluetooth

        // get bt manager
        val bluetoothManager =
            startsideFragment.context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter == null) {
            // TODO: Bluetooth not available
            Toast.makeText(
                startsideFragment.requireContext(),
                "Bluetooth støttes ikke på enhenten",
                Toast.LENGTH_SHORT
            ).show();
        } else {
            // noe bedre logikk
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

    /**
     * Returnerer en metode som brukes for å skaffe tillatelser
     *
     * Brukes for å trigge forespørsler om tillatelser fra brukeren, nødvendig for å bruke Bluetooth.
     * Sjekker deretter om tillatelser ble gitt og om SoilRoveren er tilkoblet.
     * I motsatt fall gis relevant feilmelding til brukeren.
     */
    fun permissionRequester(): ActivityResultLauncher<Array<String>> {
        return startsideFragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            // get bt manager
            bluetoothManager =
                startsideFragment.requireActivity().applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

            var perm = 0
            permissions.forEach { actionMap ->
                if (actionMap.value) {
                    perm++
                    Log.i("DEBUG", actionMap.key + " permission granted")
                } else {
                    // toast
                    Log.i("DEBUG", "permission denied")
                }
            }
            if (perm == permissions.size) {
                if (bluetoothManager.adapter?.isEnabled == false) {
                    // toast metode
                    Toast.makeText(
                        startsideFragment.requireContext(),
                        "Bluetooth er ikke slått på",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    // navn som constant
                    val bil = bluetoothManager.adapter?.bondedDevices?.filter { device ->
                        device.name.equals("HC-05")
                    }?.getOrNull(0)
                    if (bil != null) {
                        Toast.makeText(
                            startsideFragment.requireContext(),
                            "hei1",
                            Toast.LENGTH_SHORT
                        ).show()
                        ConnectThread(bil).start()
                    } else {
                        Log.i("DEBUG", "fant ikke bil!")
                        Toast.makeText(
                            startsideFragment.requireContext(),
                            "hei2",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // toast
                // TODO: permissions not given
            }
        }
    }

    /**
    // ikke her, men der du skal bruke resultatyet
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
     */

    /**
     * Tråd for å opprette Bluetoothkommunikasjon
     *
     * Da tilkobling kan ta tid, skjer dette i en egen tråd i bakgrunnen.
     * Dersom tilkoblingen er vellykket, vil en ny tråd startes som håndterer den løpende kommunikasjonen.
     *
     * @param [device] Enheten som skal kobles til
     */
    inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val uuid = device.uuids[0].uuid
            device.createRfcommSocketToServiceRecord(uuid)
        }

        /**
         * Finn Bluetoothsocket for Bluetoothenheten, og opprett kommunikasjon på denne socketen.
         */
        override fun run() {
            bluetoothManager.adapter.cancelDiscovery()
            socket?.let { s ->
                s.connect()
                bluetoothThread = ConnectedThread(s)
                bluetoothThread.start()
            }
        }

        /**
         * Lukk Bluetoothsocket for Bluetoothenheten, og avslutt tråden.
         */
        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    /**
     * Tråd for å utføre fortløpende Bluetoothkommunikasjon
     *
     * Dedikert tråd for å kommunisere over Bluetooth i bakgrunnen.
     * Håndterer både utgående og innkommende data.
     * Dette holder appen responsiv til tross for at kommunikasjonen kan være treig.
     *
     * @param [socket] Socket for kommunikasjon
     */
    inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = socket.inputStream
        private val mmOutStream: OutputStream = socket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)

        /**
         * Loop som kjøres til tråden avsluttes
         *
         * Sjekker om det finnes ny data fra kjøretøyet, og eventuelt videresender det til bluetoothHandler
         */
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
                bluetoothHandler.obtainMessage(MESSAGE_READ, message).sendToTarget()
            }
        }

        /**
         * Send data til kjøretøyet, ved neste anledning.
         *
         * @param [message] Melding som skal sendes
         */
        fun write(message: String) {
            val bytes: ByteArray = message.toByteArray()
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                return
            }
            bluetoothHandler.obtainMessage(MESSAGE_WRITE, -1, -1, mmBuffer).sendToTarget()
        }

        /**
         * Lukk Bluetoothsocket for Bluetoothenheten, og avslutt tråden.
         */
        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}