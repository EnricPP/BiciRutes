package com.example.registrerutes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.registrerutes.R
import com.example.registrerutes.db.Route
import com.example.registrerutes.other.TrackingUtility
import kotlinx.android.synthetic.main.item_personal_route.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PersonalRouteAdapter(private val personalRouteList : ArrayList<Route>) : RecyclerView.Adapter<PersonalRouteAdapter.MyViewHolder>() {

    private lateinit var listener: ItemListener


    interface ItemListener {
        fun onItemClicked(route: Route)
    }

    fun setListener(listener: ItemListener) {
        this.listener = listener;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {


        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_personal_route,
            parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val route = personalRouteList[position]


        holder.itemView.apply {

            Glide.with(this).load(route.uri).into(ivRunImage)

            tvRouteTitle.text = route.title

            val calendar = Calendar.getInstance().apply {
                timeInMillis = route.timestamp!!
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${route.avgSpeedInKMH} km/h"
            tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${route.distanceInMeters?.div(1000f)} km"
            tvDistance.text = distanceInKm

            tvTime.text = route.timeInMillis?.let { TrackingUtility.getFormattedStopWatchTime(it) }

            val caloriesBurned = "${route.caloriesBurned} kcal"
            tvCalories.text = caloriesBurned


            tvDelete.setOnClickListener {
                listener.onItemClicked(route)
            }

        }

    }

    override fun getItemCount(): Int {

        return personalRouteList.size
    }


    public class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    }

}