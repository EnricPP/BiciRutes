package com.example.registrerutes.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants.ACTION_PAUSE_SERVICE
import com.example.registrerutes.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.registrerutes.other.Constants.ACTION_STOP_SERVICE
import com.example.registrerutes.other.Constants.MAP_ZOOM
import com.example.registrerutes.other.Constants.POLYLINE_COLOR
import com.example.registrerutes.other.Constants.POLYLINE_WIDTH
import com.example.registrerutes.other.TrackingUtility
import com.example.registrerutes.services.Polyline
import com.example.registrerutes.services.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_traking.*

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_traking)  {

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L // Temps de la ruta

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener { //Botó de "Registre / Atura"
            toggleRun()
        }

        btnFinishRun.setOnClickListener{
            showEndTrackingDialog()
        }

        btnCancelRun.setOnClickListener{
            showCancelTrackingDialog()
        }

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    // Variables del fitxer "Tracking service" de les quals observem els seus canvis
    private fun subscribeToObservers() {

        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime

        })
    }

    private fun toggleRun () {
        if (isTracking) { // Pausem la ruta
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else { // Registrem
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    // Diàleg de cancel·lació  de la ruta
    private fun showCancelTrackingDialog() {

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_AppCompat_Light)
            .setTitle("Cancelar la ruta?")
            .setMessage("Alerta! Si prems Eliminar acabarà i eliminaras la ruta la ruta")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Eliminar") { _, _ ->
                stopRun()
                findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun showEndTrackingDialog() {

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_AppCompat_Light)
            .setTitle("Finalitzar la ruta?")
            .setMessage("Alerta! Si prems Finalitzar acabarà la ruta")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Finalitzar") { _, _ ->
                endRun()
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    // Acabar la ruta
    private fun endRun(){
        zoomToSeeWholeTrack()
        map?.snapshot {
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLenght(polyline).toInt() //Càlcul de la distància total
            }
            val bundle = Bundle()
            bundle.putParcelable("snapshot", it)
            bundle.putLong("time", curTimeInMillis)
            bundle.putInt("distance", distanceInMeters)
            stopRun()
            findNavController().navigate(R.id.action_trackingFragment_to_trackingInfoFragment, bundle) // Passem la captura de la ruta, el temps i la distància al "TrackingInfoFragment"
        }
    }

    // Parem la ruta
    private fun stopRun(){
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
    }

    // Depenent de si estem registrant o no, mostrem el botó de registre, aturar o finalitzar la ruta
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            btnToggleRun.text = "Registre"
            btnFinishRun.visibility = View.VISIBLE //Si no estem registrant, fem visible el botó de "Finalitzar"
        } else if (isTracking) {
            btnToggleRun.text = "Atura"
            //?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }


    // Movem la càmera a la última coordenada
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    // A partir de les coordenades podem definir el marc de la ruta acabda
    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt() // 0.05 serveix per deixar una mica més de marge
            )
        )
    }


    // Afegim totes les coordenades al mapa
    private fun addAllPolylines () {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    // Unim les dos últimes coordenades
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) { // Comprovem que hi hagin coordenades, almenys dos, en la última llista
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions() // Definim com ha de ser la línia del recorregut
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    //Definim quina acció realitzar a la funció "onStartCommand" del TrackingService
    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    //Funcions que defineixen el cicle de vida del mapa

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

}