package com.example.registrerutes.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.registrerutes.R
import com.example.registrerutes.ui.activities.FollowTrack
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_track.*


class TrackFragment : Fragment(R.layout.fragment_track) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreButton2.setOnClickListener {
            findNavController().navigate(R.id.exploreFragment)
        }

        tvDescription.movementMethod = ScrollingMovementMethod()


        //Recollim les dades que ens han passat des del Explore Fragment
        arguments?.getString("uri")?.let {
            Glide.with(this).load(it).into(ivRunImage)
        }

        arguments?.getString("description")?.let {
            tvDescription.text = it
        }

        arguments?.getString("title")?.let {
            tvTitle.text = it
        }

        followRoute.setOnClickListener {
            val intent = Intent(activity, FollowTrack::class.java)
            arguments?.getParcelableArrayList<LatLng>("coordinates")?.let {
                intent.putExtra("coordinates", it)
            }
            startActivity(intent)
        }
    }
}