package com.example.travel_application

import Trip
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import TripAdapter

//import kotlinx.android.synthetic.main.fragment_list_travel.view.* // Importuj odpowiednie widoki

class ListTravelFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_travel, container, false)

        db = FirebaseFirestore.getInstance()

        // Inicjalizacja RecyclerView
        recyclerView = view.findViewById(R.id.list_travel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Przygotowanie zapytania do Firestore
        val query = db.collection("places") // Kolekcja, którą będziesz miał w Firebase

        // Konfiguracja adaptera
        val options = FirestoreRecyclerOptions.Builder<Trip>()
            .setQuery(query, Trip::class.java)
            .build()

        adapter = TripAdapter(options)

        // Ustawienie adaptera dla RecyclerView
        recyclerView.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
