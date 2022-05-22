package com.example.soilroverapp.ui.bluetooth

import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.R
import com.example.soilroverapp.databinding.FragmentBluetoothBinding
import com.example.soilroverapp.ui.BluetoothService


const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1

class BluetoothFragment : Fragment() {

    private var _binding: FragmentBluetoothBinding? = null

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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bluetoothButton: Button = requireView().findViewById(R.id.bluetoothbutton)

        bluetoothButton.setOnClickListener {
            BluetoothService.getInstance().kobleTilBluetooth(this)
        }

        val butt:Button = requireView().findViewById((R.id.button))

        butt.setOnClickListener {
            val message: String = "fisk"
            BluetoothService.getInstance().bluetoothThread.write(message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}