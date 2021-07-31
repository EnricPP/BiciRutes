package com.example.registrerutes.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_group.*

class GroupFragment : Fragment(R.layout.fragment_group){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statisticsButton.setOnClickListener {
            navHostFragment.findNavController().navigate(R.id.action_groupFragment_to_personalFragment)
        }
    }
}