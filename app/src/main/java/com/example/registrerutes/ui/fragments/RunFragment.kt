package com.example.registrerutes.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.registrerutes.R
import com.example.registrerutes.adapters.PersonalRouteAdapter
import com.example.registrerutes.db.Route
import com.example.registrerutes.other.Constants
import com.example.registrerutes.other.Constants.KEY_MAIL
import com.example.registrerutes.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.registrerutes.other.TrackingUtility
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks, PersonalRouteAdapter.ItemListener {

    private lateinit var db : FirebaseFirestore
    private lateinit var userRecyclerview : RecyclerView
    private lateinit var personalRouteAdapter : PersonalRouteAdapter
    private lateinit var routeArrayList : ArrayList<Route>

    private var sortBy : String = "timestamp"
    val storage = Firebase.storage


    @Inject
    lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = sharedPreferences.getString(Constants.KEY_MAIL, null)

        if (email == null){
            navHostFragment.findNavController().navigate(R.id.loginFragment)
        } else {
            requestPermissions()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        userRecyclerview = rvRuns
        userRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerview.setHasFixedSize(true)
        routeArrayList = arrayListOf<Route>()
        personalRouteAdapter = PersonalRouteAdapter(routeArrayList)
        userRecyclerview.adapter = personalRouteAdapter
        personalRouteAdapter.setListener(this@RunFragment)

        getSortType()
    }

    private fun EventChangeListener() {

        routeArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("routes").whereEqualTo("user", sharedPreferences.getString(KEY_MAIL, null)).orderBy(sortBy, Query.Direction.DESCENDING) //Recollim les rutes de l'usuari actual
            .addSnapshotListener(object : EventListener<QuerySnapshot>{
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                        if (error != null){
                            Log.e("Firestore Error", error.message.toString())
                            return
                        }
                        for (dc : DocumentChange in value?.documentChanges!!){
                            if (dc.type == DocumentChange.Type.ADDED){
                                routeArrayList.add(dc.document.toObject(Route::class.java))
                            }
                        }
                    personalRouteAdapter.notifyDataSetChanged()
                }
            })
    }

    private fun getSortType(){

        spFilter.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, pos: Int, id: Long
            ) {
                when(pos) {
                    0 -> sortBy = "timestamp"
                    1 -> sortBy = "timeInMillis"
                    2 -> sortBy = "distanceInMeters"
                    3 -> sortBy = "avgSpeedInKMH"
                    4 -> sortBy = "caloriesBurned"
                }
                EventChangeListener()

            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {
            }
        })
    }


    // Demanem els permisos de localitzaci?? a l'usuari
    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(requireContext())) { //Ja els ha acceptat anteriorment
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "Has d???acceptar els permisos d???ubicaci?? per utilizar aquesta aplicaci??",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        else {
            EasyPermissions.requestPermissions(
                this,
                "Has d???acceptar els permisos d???ubicaci?? utilizar aquesta aplicaci??",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    //Si l'usuari accepta els permissos no cal fer res mes
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }


    //Si l'usuari no accepta alguns dels permissos, li tornem a demanar
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
        else {
            requestPermissions()
        }
    }

    //Especifiquem quin ??s el fragment que rep els resultats del permissos, en aquest cas "this"
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onItemClicked(route: Route) {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_AppCompat_Light)
            .setTitle("Eliminar la ruta: " + route.title)
            .setMessage("Alerta! Vols eliminar la ruta?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Eliminar") { _, _ ->

                db = FirebaseFirestore.getInstance()
                route.key?.let {

                    //Eliminem la captura de la ruta (storage)
                    val storageRef = storage.reference
                    val desertRef = storageRef.child("routes_caps/${route.title}-$it")
                    desertRef.delete()

                    //Eliminem la ruta (bdd)
                    db.collection("routes").document("/" + it)
                        .delete()
                        .addOnSuccessListener {
                            personalRouteAdapter.notifyDataSetChanged()
                            Snackbar.make(
                                this.requireView(),
                                "Ruta eliminada correctament",
                                Snackbar.LENGTH_SHORT).show()
                            EventChangeListener()
                        }
                        .addOnFailureListener { e -> Log.w("Delete", "Error deleting document", e) }
                }
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }
}