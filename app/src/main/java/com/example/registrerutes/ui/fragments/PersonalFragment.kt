package com.example.registrerutes.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants.KEY_MAIL
import com.example.registrerutes.other.Constants.KEY_WEIGHT
import com.example.registrerutes.other.TrackingUtility
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.fragment_personal.etWeight
import java.lang.Math.round
import javax.inject.Inject



@AndroidEntryPoint
class PersonalFragment : Fragment(R.layout.fragment_personal) {


    private val db = FirebaseFirestore.getInstance()

    @Inject
    lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = sharedPreferences.getString(KEY_MAIL, null)

        if (email == null){
            navHostFragment.findNavController().navigate(R.id.loginFragment)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        logoutButton.setOnClickListener {
            sharedPreferences.edit()
                .clear()
                .apply()
            FirebaseAuth.getInstance().signOut()
            navHostFragment.findNavController().navigate(R.id.exploreFragment)
        }

        groupsButton.setOnClickListener {
            navHostFragment.findNavController().navigate(R.id.action_personalFragment_to_groupFragment)
        }

        //Recollim les estadístiques totals de l'usuari i les passem als seus respectius textview
        totalStatistics() { time: Int ,distance: Int, total_avg_speed: Float ,calories: Int ->
            val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(time.toLong()) //Passem el temps en milisegons al format (00:00:00)
            tvTotalTime.text = totalTimeRun

            val avgSpeed = round(total_avg_speed * 10f) / 10f
            val avgSpeedString = "${avgSpeed}km/h"
            tvAverageSpeed.text = avgSpeedString

            val km = distance / 1000f //Passem la distància a Km
            val totalDistance = round(km * 10f) / 10f
            val totalDistanceString = "${totalDistance}km"
            tvTotalDistance.text = totalDistanceString

            val totalCalories = "${calories}kcal"
            tvTotalCalories.text = totalCalories

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

        //Actualitzem el pes de l'usuari a la bdd
        sharedPreferences.getString(KEY_MAIL, "")?.let {
            db.collection("users").document(it).update("weight", weightText)
        }

        return true
    }


    fun totalStatistics(myCallback: (Int, Int, Float, Int) -> Unit) {

        var total_time = 0
        var total_distance = 0
        var total_avg_speed = 0F
        var total_calories = 0
        var total = 0


        db.collection("routes").whereEqualTo("user", sharedPreferences.getString(KEY_MAIL, null)).get().addOnCompleteListener { routes ->
            if (routes.isSuccessful) {
                for (document in routes.result!!) {
                    total_time += document.data["timeInMillis"].toString().toInt()
                    total_distance += document.data["distanceInMeters"].toString().toInt()
                    total_avg_speed += document.data["avgSpeedInKMH"].toString().toFloat()
                    total_calories += document.data["caloriesBurned"].toString().toInt()
                    total += 1

                }
                myCallback(total_time, total_distance, total_avg_speed/total, total_calories)
            }
        }
    }

}


