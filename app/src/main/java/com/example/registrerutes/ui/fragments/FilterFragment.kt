package com.example.registrerutes.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.exploreButton2



class FilterFragment : Fragment(R.layout.fragment_filter) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreButton2.setOnClickListener {
            findNavController().navigate(R.id.exploreFragment)
        }

        val modalitiesArray = resources.getStringArray(R.array.modality)
        val adapterModality = ArrayAdapter(requireContext(), R.layout.list_item, modalitiesArray)
        modalitiesFilter.setAdapter(adapterModality)


        val dificultiesArray = resources.getStringArray(R.array.dificulty)
        val adapterDificulty = ArrayAdapter(requireContext(), R.layout.list_item, dificultiesArray)
        dificultiesFilter.setAdapter(adapterDificulty)

        applyFilter.setOnClickListener {

            val bundle = Bundle()

            bundle.putString("dificulty", dificultiesFilter.text.toString())
            findNavController().navigate(R.id.exploreFragment,bundle)

            bundle.putString("modality", modalitiesFilter.text.toString())
            findNavController().navigate(R.id.exploreFragment,bundle)

            bundle.putString("title", titleFilter.text.toString())
            findNavController().navigate(R.id.exploreFragment,bundle)
        }

    }


}