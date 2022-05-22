package com.example.soilroverapp.ui.startside

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soilroverapp.databinding.FragmentFjernstyringBinding


/**
 * Klasse for å kunne styre kjøretøyet
 *
 * Orientasjon blir endret til landscape og to joystick blir laget
 */
class StartsideFragment : Fragment() {

    val MESSAGE_READ: Int = 0
    val MESSAGE_WRITE: Int = 1
    private var _binding: FragmentFjernstyringBinding? = null
    private val binding get() = _binding!!

    /**
     * Metode som lager Viewet og endrer til landscape
     *
     * @param [LayoutInflater] inflater
     * @param [ViewGroup] container
     * @param [Bundle] savedInstanceState
     * @see [Layout] fragment_fjernstyring
     * @return root
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fjernstyringViewModel =
            ViewModelProvider(this)[StartsideViewModel::class.java]

        _binding = FragmentFjernstyringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activity?.apply {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        return root
    }

    /**
     * Nytt vindu så endres orientasjon til portrait igjen
     *
     * Når man velger et nytt vindu fra menyen så endres orientasjoen til portrait
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        activity?.apply {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}