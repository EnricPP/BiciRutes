package com.example.registrerutes.db

data class Route(var title: String ? = "",
                 val timestamp: Long ? = 0,
                 val description: String ? = "",
                 val avgSpeedInKMH: Float ? = 0F,
                 val distanceInMeters: Int ? = 0,
                 val timeInMillis: Long ? = 0,
                 val caloriesBurned: Int ? = 0,
                 val modality: String ? = "",
                 val dificulty: String ? = "",
                 val user: String ? = "",
                 val uri: String ? = "",
                 val key: String ? = "")
