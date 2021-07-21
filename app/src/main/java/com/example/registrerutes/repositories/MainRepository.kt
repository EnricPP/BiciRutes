package com.example.registrerutes.repositories

import com.example.registrerutes.db.Run
import com.example.registrerutes.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO
){
    suspend fun insertRun (run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun (run: Run) = runDAO.deleteRun(run)

    fun getTotalTimeInMillis () = runDAO.getTotalTimeInMillis()

    fun getTotalDistance () = runDAO.getTotalDistance()

    fun getTotalCalories() = runDAO.getTotalCalories()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()

}