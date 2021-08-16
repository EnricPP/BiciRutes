package com.example.registrerutes.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.registrerutes.R
import com.example.registrerutes.adapters.ExploreAdapter
import com.example.registrerutes.db.Route
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_explore.*


class ExploreFragment : Fragment(R.layout.fragment_explore), ExploreAdapter.ItemListener {

    private lateinit var db : FirebaseFirestore
    private lateinit var exploreRecyclerview : RecyclerView
    private lateinit var exploreAdapter : ExploreAdapter
    private lateinit var routeArrayList : ArrayList<Route>
    private var coordinates = arrayListOf<LatLng>()

    private lateinit var lastResult: DocumentSnapshot

    private var dificulty: String = ""
    private var modality: String = ""
    private var title: String = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreRecyclerview = rvExplore
        exploreRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        exploreRecyclerview.setHasFixedSize(true)
        routeArrayList = arrayListOf<Route>()
        exploreAdapter = ExploreAdapter(routeArrayList)
        exploreRecyclerview.adapter = exploreAdapter
        exploreAdapter.setListener(this@ExploreFragment)


        routeArrayList.clear()
        db = FirebaseFirestore.getInstance()

        arguments?.getString("dificulty")?.let {
            this.dificulty = it
        }

        arguments?.getString("modality")?.let {
            this.modality = it
        }

        arguments?.getString("title")?.let {
            this.title = it
        }

        EventChangeListener()

        loadRoutes.setOnClickListener {
            EventChangeListener()
        }

        filterRoutes.setOnClickListener{
            findNavController().navigate(R.id.filterFragment)
        }

    }

    private fun EventChangeListener() {

        var query : Query

        query = db.collection("routes")
        query = query.orderBy("timestamp", Query.Direction.DESCENDING)

        if (routeArrayList.isEmpty()) {
            if (dificulty != "")
                query = query.whereEqualTo("dificulty", dificulty)
            if(modality != "")
                query = query.whereEqualTo("modality", modality)
            query = query.limit(5)

        } else {
            if (dificulty != "")
                query = query.whereEqualTo("dificulty", dificulty)
            if(modality != "")
                query = query.whereEqualTo("modality", modality)
            query = query.startAfter(lastResult).limit(5)
        }

        query.addSnapshotListener(object : EventListener<QuerySnapshot> {
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
                            if (title.isNullOrEmpty())
                                routeArrayList.add(dc.document.toObject(Route::class.java))
                            else{
                                if (dc.document["title"].toString().contains(title))
                                    routeArrayList.add(dc.document.toObject(Route::class.java))
                            }
                        }
                    }
                    if (value.size() > 0) {
                        lastResult = value.documents.get(value.size() - 1)
                        exploreAdapter.notifyDataSetChanged()
                    }
                    else{
                        Snackbar.make(
                            requireView(),
                            "No hi han més rutes per mostrar",
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
    }


    override fun onItemClicked(route: Route) {

        val bundle = Bundle()

        for(pos in route.coordinates!!){
            val coord = LatLng(pos.latitude, pos.longitude)
            coordinates.add(coord)
        }


        bundle.putParcelableArrayList("coordinates", coordinates)
        bundle.putString("uri", route.uri)
        bundle.putString("description", route.description)
        bundle.putString("title", route.title)

        findNavController().navigate(R.id.trackFragment, bundle)
    }


}

