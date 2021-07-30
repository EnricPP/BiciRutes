package com.example.registrerutes.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.registrerutes.other.Constants.KEY_MAIL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var sharedPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(toolbar)


        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /* Res */ } //Si cliquem la icona del menu de la pantalla on estem, no volem que faci res

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id) {
                    R.id.exploreFragment, R.id.runFragment, R.id.personalFragment -> //Definim en quins Fragment apareix la barra de navegació
                        bottomNavigationView.visibility = View.VISIBLE
                    else ->  bottomNavigationView.visibility = View.GONE
                }
            }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    // Cas on l'usuari clica la notificació de registre. El redirigim cap a la pantalla de registre.
    private fun navigateToTrackingFragmentIfNeeded (intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }

}