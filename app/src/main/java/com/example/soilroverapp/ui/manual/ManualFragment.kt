package com.example.soilroverapp.ui.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.R
import com.example.soilroverapp.databinding.FragmentManualBinding
import com.github.barteksc.pdfviewer.PDFView


class ManualFragment : Fragment() {

    var pdfView: PDFView? = null
    private var _binding: FragmentManualBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val manualViewModel =
            ViewModelProvider(this).get(ManualViewModel::class.java)

        _binding = FragmentManualBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pdfView = requireView().findViewById(R.id.pdfView1) as PDFView
        pdfView!!.fromAsset("PM utstilling 2022.pdf").load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}