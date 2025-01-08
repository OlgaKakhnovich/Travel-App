package com.example.travel_application

import PlaceAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


class NewsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var placeAdapter: PlaceAdapter
    private val placeList = mutableListOf<Place>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_news, container, false)
        recyclerView = view.findViewById(R.id.list_post)
        recyclerView.layoutManager = LinearLayoutManager(context)
        placeAdapter = PlaceAdapter(placeList)
        recyclerView.adapter = placeAdapter

        fetchPlaces()

        return view
    }

    private fun fetchPlaces() {
        val db = FirebaseFirestore.getInstance()
        db.collection("places")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val place = document.toObject(Place::class.java)
                    placeList.add(place)
                }
                placeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseError", "Error fetching places", exception)
            }
    }

}