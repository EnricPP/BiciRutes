package com.example.registrerutes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.example.registrerutes.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_tracking_info.*

class TrackingInfoFragment : Fragment (R.layout.fragment_tracking_info) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dificultiesArray = resources.getStringArray(R.array.dificulty)
        val modalitiesArray = resources.getStringArray(R.array.modality)

        val adapterDificulty = ArrayAdapter(requireContext(), R.layout.list_item, dificultiesArray)
        val adapterModality = ArrayAdapter(requireContext(), R.layout.list_item, modalitiesArray)

        dificulties.setAdapter(adapterDificulty)
        modalities.setAdapter(adapterModality)
    }


}