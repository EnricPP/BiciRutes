package com.example.registrerutes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.registrerutes.R
import com.example.registrerutes.db.Route
import kotlinx.android.synthetic.main.item_explore.view.*
import kotlinx.android.synthetic.main.item_personal_route.view.ivRunImage
import kotlinx.android.synthetic.main.item_personal_route.view.tvDistance
import kotlin.collections.ArrayList

class ExploreAdapter(private val exploreRouteList : ArrayList<Route>) : RecyclerView.Adapter<ExploreAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_explore,
            parent,false)
        return ExploreAdapter.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExploreAdapter.MyViewHolder, position: Int) {
        val route = exploreRouteList[position]


        holder.itemView.apply {

            Glide.with(this).load(route.uri).into(ivRunImage)

            tvRouteExpTitle.text = route.title

            tvModality.text  = route.modality

            tvDificulty.text = route.dificulty

            val distanceInKm = "Ditància: ${route.distanceInMeters?.div(1000f)} km"
            tvDistance.text = distanceInKm

            if(position == exploreRouteList.size){

            }

        }
    }

    override fun getItemCount(): Int {
        return exploreRouteList.size
    }

    public class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    }


}

