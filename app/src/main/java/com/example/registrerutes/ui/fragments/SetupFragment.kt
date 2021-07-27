package com.example.registrerutes.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.registrerutes.other.Constants.KEY_NAME
import com.example.registrerutes.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true


    // Comprovem si és la primera vegada que l'usuari introdueix en nom i pes. Si no és la primera vegada el redirigim cap a la pantalla de explora
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.exploreFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_exploreFragment,
                savedInstanceState,
                navOptions)
        }

        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref() //Comprovem que l'usuari hagi entrar les dades

            if (success)
                findNavController().navigate(R.id.action_setupFragment_to_exploreFragment)
            else {
                Snackbar.make(requireView(), "Siusplau emplena tots els camps", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // Guardem el nom i pes
    private fun writePersonalDataToSharedPref(): Boolean {
        val name = etName.text.toString()
        val weight = etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false) //No és la primera vegada que l'usuari entra les dades
            .apply()
        return true
    }
}