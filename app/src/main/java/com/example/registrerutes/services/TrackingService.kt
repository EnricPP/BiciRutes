package com.example.registrerutes.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.registrerutes.R
import com.example.registrerutes.other.Constants.ACTION_PAUSE_SERVICE
import com.example.registrerutes.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.registrerutes.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.registrerutes.other.Constants.ACTION_STOP_SERVICE
import com.example.registrerutes.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.registrerutes.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.registrerutes.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.registrerutes.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.registrerutes.other.Constants.NOTIFICATION_ID
import com.example.registrerutes.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.registrerutes.other.TrackingUtility
import com.example.registrerutes.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polilines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService(){

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient // provideFusedLocationProviderClient del fitxer "Service Module"

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder //Notification builder del fitxer "Service Module"

    lateinit var curNotificationBuilder: NotificationCompat.Builder //Notificació actual, igual que la base (baseNotificationBuilder) però amb el timer canviat

    companion object { //valors que volem observar els seus canvis des del Tracking Fragment
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polilines>() // Llista de llistes de coordenades, cada vegada que pausem el registre, les següents coordenades s'afageixen a una nova llista
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder // Inicialment la notificació actual és la mateixa que la base
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer { // Obervem si hi han canvis de la variable "isTracking"
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    // Valors inicials
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    // Aturar el servei
    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) { // Si és la primera vegada que l'usuari clica "Registrar", posem en marxa el servei de registre en segon plà
                        startForegroundService()
                        isFirstRun = false
                    }
                    else{
                        Timber.d("Resuming Service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("PAUSED SERVICE")
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                    Timber.d("STOPED SERVICE")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L // Temps des de que iniciem el registre fins que el pausem
    private var timeRun = 0L // Temps total de la ruta
    private var timeStarted = 0L // Hora en que començem la ruta
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) { // Mentres estem registrant
                lapTime = System.currentTimeMillis() - timeStarted // Diferència de temps entre l'hora actual i l'hora en que hem començat la ruta
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) { // Comprovem si ha passat un nou segon registrant
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1) // Afegim un segon al la variable del temps total en segons
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL) // Definim un delay per la corutina
            }
            timeRun += lapTime // Si no estem registrant, actualitzem el temps total de la ruta
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    // Funció on actualitzem la notificació actual i la mostrem
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText =
            if (isTracking) "Pausar" // Si estem registrant volem poder pausar la ruta des de la notificació
            else "Reprendre" // Contràriament la volem poder reprendre

        val pendingIntent = if (isTracking) { // Pausem el registre
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else { // Reprenem el registre
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply { // Si l'usuari té els permisos de localització, volem rebre actualitzacions de la localització
                    interval = LOCATION_UPDATE_INTERVAL // Definim cada quan rebem notificacions de localització, en aquest cas cada cinc segons
                    fastestInterval = FASTEST_LOCATION_INTERVAL // Definim l'interval més ràpid en que podem rebre notificacions de localització, en aquest cas cada dos segons
                    priority = PRIORITY_HIGH_ACCURACY //
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else { // Si no estem registrant, no volem rebre notificacions de localització
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    // Afegim la localització al final del últim Polyline
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    // Afegim una nova localització (coordenada) a la llista
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude,location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    // Quan pausem el servei de registre, afegim una llista buida
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // inicalitzem la llista de llistes de coordenades


    // Comença el servei de registre en segon plà
    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager as NotificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if (!serviceKilled) { // Assegurem que el servei estigui en marxa
            var notification = curNotificationBuilder // Actualitzem la notificació actual (cada segon)
                .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
            notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    // Notificació que avisa a l'usuari que la ruta s'està registrant
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        var channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }

}