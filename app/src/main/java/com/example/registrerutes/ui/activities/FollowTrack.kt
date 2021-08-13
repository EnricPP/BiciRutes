package com.example.registrerutes.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint


class FollowTrack : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var map: GoogleMap
    private var coordinates = arrayListOf<LatLng>()

    companion object{
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_track)
        createFragment()

        coordinates = intent.getParcelableArrayListExtra("coordinates")
    }

    private fun createFragment() {
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createPolylines()
        createMarker()
        enableLocation()
    }


    private fun createMarker() {
        val favoritePlace = coordinates.first()
        val favoritePlace2 = coordinates.last()
        map.addMarker(MarkerOptions().position(favoritePlace).title("Inici")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN ))
        )

        map.addMarker(
            MarkerOptions()
                .position(favoritePlace2)
                .title("Final")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, Constants.MAP_ZOOM),
            3000,
            null
        )
    }


    private fun createPolylines(){
        val polylineOptions = PolylineOptions()
            .addAll(coordinates)
            .width(Constants.POLYLINE_WIDTH)
            .color(ContextCompat.getColor(this, R.color.route))

        val polyline = map.addPolyline(polylineOptions)
        polyline.startCap = RoundCap()

    }


    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()){
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }



    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "Accepta els permisos de localització", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Accepta els permisos de localització", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    override fun onResumeFragments() {
        if (!::map.isInitialized) return
        if(!isLocationPermissionGranted()){
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Accepta els permisos de localització", Toast.LENGTH_SHORT).show()
        }
    }

}