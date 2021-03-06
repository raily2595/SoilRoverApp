package com.example.soilroverapp.ui.startside

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.widget.Button
import com.example.soilroverapp.databinding.FragmentStartsideBinding

class StartsideFragment : Fragment() {

    private var _binding: FragmentStartsideBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val startsideViewModel =
            ViewModelProvider(this).get(StartsideViewModel::class.java)

        _binding = FragmentStartsideBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}