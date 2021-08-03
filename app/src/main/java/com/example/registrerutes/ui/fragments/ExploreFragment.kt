package com.example.registrerutes.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.registrerutes.R
import com.example.registrerutes.adapters.ExploreAdapter
import com.example.registrerutes.adapters.PersonalRouteAdapter
import com.example.registrerutes.db.Route
import com.example.registrerutes.other.Constants
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_explore.*
import kotlinx.android.synthetic.main.fragment_run.*

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private lateinit var db : FirebaseFirestore
    private lateinit var exploreRecyclerview : RecyclerView
    private lateinit var exploreAdapter : ExploreAdapter
    private lateinit var routeArrayList : ArrayList<Route>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreRecyclerview = rvExplore
        exploreRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        exploreRecyclerview.setHasFixedSize(true)
        routeArrayList = arrayListOf<Route>()
        exploreAdapter = ExploreAdapter(routeArrayList)
        exploreRecyclerview.adapter = exploreAdapter

        EventChangeListener()

    }

    private fun EventChangeListener() {

        routeArrayList.clear()


        db = FirebaseFirestore.getInstance()

        db.collection("routes").orderBy("timestamp", Query.Direction.DESCENDING) //Recollim totes les rutes
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
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
                    exploreAdapter.notifyDataSetChanged()
                }
            })
    }


}

