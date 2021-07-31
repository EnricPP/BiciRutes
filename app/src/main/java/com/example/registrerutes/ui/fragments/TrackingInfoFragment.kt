package com.example.registrerutes.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import com.example.registrerutes.db.Run
import com.example.registrerutes.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking_info.*
import java.io.ByteArrayOutputStream
import java.lang.Math.round
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class TrackingInfoFragment : Fragment (R.layout.fragment_tracking_info) {

    private val viewModel: MainViewModel by viewModels()
    private var distance: Int = 0
    private var time: Long = 0L
    private lateinit var snapshot: Bitmap

    @set:Inject
    var weight = 80f


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val dificultiesArray = resources.getStringArray(R.array.dificulty)
        val modalitiesArray = resources.getStringArray(R.array.modality)

        val adapterDificulty = ArrayAdapter(requireContext(), R.layout.list_item, dificultiesArray)
        val adapterModality = ArrayAdapter(requireContext(), R.layout.list_item, modalitiesArray)

        dificulties.setAdapter(adapterDificulty)
        modalities.setAdapter(adapterModality)



        //Recollim les dades que ens han passat des del Tracking Fragment
        arguments?.getInt("distance")?.let {
            this.distance = it
        }

        arguments?.getLong("time")?.let {
            this.time = it
        }

        arguments?.getParcelable<Bitmap>("snapshot")?.let {
            this.snapshot = it
        }

        saveRun.setOnClickListener{
            endRunAndSaveToDb()
        }

    }


    //Guardem la ruta a la bdd
    private fun endRunAndSaveToDb () {
        val avgSpeed = round((distance / 1000f) / (time / 1000f / 60 / 60)  * 10 ) / 10f
        val dateTimestamp = Calendar.getInstance().timeInMillis
        var caloriesBurned = ((distance / 1000f) * weight).toInt() // càlcul de les calories
        val title = tvTitle.text.toString()
        var description = tvDescription.text.toString()
        var dificulty = dificulties.text.toString()
        var modality = modalities.text.toString()

        if (title.length > 25) {
            Snackbar.make(requireView(), "El títol no pot superar els 25 caràcters", Snackbar.LENGTH_SHORT).show()
        }
        else {
            if(title.isEmpty() || description.isEmpty() || dificulty.isEmpty() || modality.isEmpty()) {
                Snackbar.make(requireView(), "Siusplau emplena tots els camps", Snackbar.LENGTH_SHORT).show()
            } else {
                val curTime = System.currentTimeMillis()
                saveSnapshotToDb(title, curTime)
                val run = Run(snapshot, dateTimestamp, avgSpeed, distance, time, caloriesBurned, title, description, dificulty, modality)

                viewModel.insertRun(run)
                Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Ruta guardada correctament!",
                    Snackbar.LENGTH_LONG).show()

                findNavController().navigate(R.id.action_trackingInfoFragment_to_runFragment)
            }
        }
    }

    private fun saveSnapshotToDb(title: String, curTime: Long) {
        val baos = ByteArrayOutputStream()
        snapshot.compress(Bitmap.CompressFormat.PNG,100,baos)
        val data = baos.toByteArray()

        val ref = FirebaseStorage.getInstance().getReference("/routes_caps/$title-$curTime")
        ref.putBytes(data)
    }

}