package com.example.registrerutes.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.registrerutes.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel(){

    val totalTime = mainRepository.getTotalTimeInMillis()
    var totalDistance = mainRepository.getTotalDistance()
    val totalCalories = mainRepository.getTotalCalories()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

}