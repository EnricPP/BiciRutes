package com.example.registrerutes.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants.KEY_WEIGHT
import com.example.registrerutes.other.TrackingUtility
import com.example.registrerutes.ui.viewmodels.StatisticsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_personal.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class PersonalFragment : Fragment(R.layout.fragment_personal) {

    private val viewModel: StatisticsViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        loadFieldsFromSharedPref()

        // Actualitzem el pes
        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Pes actualitzat correctament", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(view, "Siusplau entra un pes", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref () {
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean{
        val weightText = etWeight.text.toString()

        if(weightText.isEmpty())
        {
            return false
        }
        sharedPreferences.edit()
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()

        return true
    }


    private fun subscribeToObservers() {
        viewModel.totalTime.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it) //Passem el temps en milisegons al format (00:00:00)
                tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f //Passem la distància a Km
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.totalCalories.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                tvTotalCalories.text = totalCalories
            }
        })
    }
}