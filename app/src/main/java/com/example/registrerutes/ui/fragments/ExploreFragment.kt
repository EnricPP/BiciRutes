package com.example.registrerutes.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.registrerutes.R
import com.example.registrerutes.adapters.ExploreAdapter
import com.example.registrerutes.db.Route
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_explore.*


class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private lateinit var db : FirebaseFirestore
    private lateinit var exploreRecyclerview : RecyclerView
    private lateinit var exploreAdapter : ExploreAdapter
    private lateinit var routeArrayList : ArrayList<Route>

    private lateinit var lastResult: DocumentSnapshot

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreRecyclerview = rvExplore
        exploreRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        exploreRecyclerview.setHasFixedSize(true)
        routeArrayList = arrayListOf<Route>()
        exploreAdapter = ExploreAdapter(routeArrayList)
        exploreRecyclerview.adapter = exploreAdapter


        routeArrayList.clear()
        db = FirebaseFirestore.getInstance()
        EventChangeListener()

        loadRoutes.setOnClickListener {
            EventChangeListener()
        }
    }

    private fun EventChangeListener() {

        var query : Query

        if (routeArrayList.isEmpty()) {
            query = db.collection("routes").orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
        } else {
            query = db.collection("routes").orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastResult)
                .limit(5)
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
                            routeArrayList.add(dc.document.toObject(Route::class.java))
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


}

