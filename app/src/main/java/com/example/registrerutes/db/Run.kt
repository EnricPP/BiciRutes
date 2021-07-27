package com.example.registrerutes.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var img: Bitmap? = null, //imatge de la ruta
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0,
    var title: String = "",
    var description: String = "",
    val dificulty: String = "",
    var modality: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}