package com.example.soilroverapp.ui.omoss

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.R
import com.example.soilroverapp.databinding.FragmentOmossBinding


class OmossFragment : Fragment() {

    private var _binding: FragmentOmossBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val omossViewModel =
            ViewModelProvider(this).get(OmossViewModel::class.java)

        _binding = FragmentOmossBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val soilSteam: Button = requireView().findViewById(R.id.soilsteamknapp)
        val soilSprouts: Button = requireView().findViewById((R.id.soilsproutsknapp))

        soilSteam.setOnClickListener {
            val Getintent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://soilsteam.com/"))
            startActivity(Getintent)
        }

        soilSprouts.setOnClickListener {
            val Getintent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://web01.usn.no/~229456/"))
            startActivity(Getintent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}