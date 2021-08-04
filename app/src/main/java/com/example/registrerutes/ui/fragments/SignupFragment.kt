package com.example.registrerutes.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_signup.tvMail
import kotlinx.android.synthetic.main.fragment_signup.tvPassword

class SignupFragment : Fragment(R.layout.fragment_signup) {

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        backToLoginButton.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        signupButton.setOnClickListener {
            if (tvUser.text.isNullOrEmpty() || tvMail.text.isNullOrEmpty() || tvPassword.text.isNullOrEmpty() || tvWeight.text.isNullOrEmpty()) {
                Snackbar.make(requireView(), "Siusplau emplena tots els camps", Snackbar.LENGTH_SHORT).show()
            } else {
                if (tvUser.text.toString().length > 15) {
                    Snackbar.make(requireView(), "L'usuari no pot superar els 15 caràcters", Snackbar.LENGTH_SHORT).show()
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(tvMail.text.toString(), tvPassword.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                db.collection("users").document(tvMail.text.toString()).set(
                                    hashMapOf(
                                        "username" to tvUser.text.toString(),
                                        "weight" to tvWeight.text.toString()
                                    )
                                )
                                findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                            } else {
                                Snackbar.make(requireView(), "Error en el registre", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}