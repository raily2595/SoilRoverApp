package com.example.soilroverapp.ui.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.R
import com.example.soilroverapp.databinding.FragmentManualBinding
import com.example.soilroverapp.ui.omoss.OmossViewModel
import com.github.barteksc.pdfviewer.PDFView


class ManualFragment : Fragment() {

    private var pdfView: PDFView? = null
    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!

    /**
     * Skjer når viewet skal til å settes opp
     *
     * Setter opp PDF-fremviseren
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Skjer når viewet er satt opp
     *
     * Setter fremviseren til å vise PDF med filnavnet som ligger i ManualViewModel
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pdfView = requireView().findViewById(R.id.pdfView1) as PDFView
        val pdfObserver = Observer<String> { pdfFile ->
            pdfView!!.fromAsset(pdfFile).load()
        }
        val manualViewModel = ViewModelProvider(this)[ManualViewModel::class.java]
        manualViewModel.pdfFile.observe(viewLifecycleOwner, pdfObserver)
    }

    /**
     * Skjer når viewet skal lukkes
     *
     * Nullstiller fremviseren
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}