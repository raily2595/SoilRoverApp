package com.example.soilroverapp.ui.fjernstyring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.databinding.FragmentFjernstyringBinding

class FjernFragment : Fragment() {

    private var _binding: FragmentFjernstyringBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fjernstyringViewModel =
            ViewModelProvider(this).get(FjernstyringViewModel::class.java)

        _binding = FragmentFjernstyringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textFjernstyring
        fjernstyringViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}