package com.example.registrerutes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.registrerutes.R
import com.example.registrerutes.db.Run
import com.example.registrerutes.other.TrackingUtility
import com.example.registrerutes.ui.MainActivity
import com.example.registrerutes.ui.fragments.RunFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    private lateinit var listener: ItemListener

    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface ItemListener {
        fun onItemClicked(run: Run, position: Int)
    }

    fun setListener(listener: ItemListener) {
        this.listener = listener;
    }

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    var differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]

        holder.itemView.apply {
            Glide.with(this).load(run.img).into(ivRunImage)

            tvTitle.text = run.title

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH} km/h"
            tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f} km"
            tvDistance.text = distanceInKm

            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned} kcal"
            tvCalories.text = caloriesBurned

            tvDelete.setOnClickListener(View.OnClickListener {  view ->
                listener.onItemClicked(run,position)
                notifyDataSetChanged()
            })
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    private fun showDeleteRunDialog(context: Context, position: Int) {
        val run = differ.currentList[position]
        val dialog = MaterialAlertDialogBuilder(context, R.style.AlertDialog_AppCompat_Light)
            .setTitle("Eliminar la ruta")
            .setMessage("Alerta! Vols eliminar la ruta?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Eliminar") { _, _ ->
                differ.currentList.toMutableList().removeAt(position)
                notifyItemRemoved(position)
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }
}