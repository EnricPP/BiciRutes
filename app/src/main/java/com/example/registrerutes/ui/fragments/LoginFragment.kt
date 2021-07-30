package com.example.registrerutes.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.registrerutes.other.Constants.KEY_MAIL
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        exploreButton.setOnClickListener {
            findNavController().navigate(R.id.exploreFragment)
        }

        loginButton.setOnClickListener {
            if (tvMail.text.isNullOrEmpty() || tvPassword.text.isNullOrEmpty()){
                Snackbar.make(requireView(), "Siusplau emplena tots els camps", Snackbar.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(tvMail.text.toString(), tvPassword.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            writePersonalDataToSharedPref(tvMail.text.toString())
                            findNavController().navigate(R.id.exploreFragment)
                        } else {
                            Snackbar.make(requireView(), "Error en les credencials: Correu o contrasenya incorrectes", Snackbar.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    // Guardem el correu electrònic a les shared preferences
    private fun writePersonalDataToSharedPref(email: String) {
        sharedPref.edit()
            .putString(KEY_MAIL, email)
            .apply()
    }
}