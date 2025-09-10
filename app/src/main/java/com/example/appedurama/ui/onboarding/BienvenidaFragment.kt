package com.example.appedurama.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.appedurama.R
import androidx.navigation.fragment.findNavController
import com.example.appedurama.databinding.FragmentPreguntasInicioBinding

class BienvenidaFragment : Fragment() {


    private var _binding: FragmentPreguntasInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreguntasInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnEncuentraCamino.setOnClickListener {

            findNavController().navigate(R.id.action_bienvenidaFragment_to_navigation_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
