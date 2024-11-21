package com.example.travel_application

import Trip
import TripAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ListTravelFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var adapter: TripAdapter? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_travel, container, false)

        db = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.list_travel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {

            val query = db.collection("places")
                .whereEqualTo("userId", currentUser.uid)


            val options = FirestoreRecyclerOptions.Builder<Trip>()
                .setQuery(query, Trip::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

            adapter = TripAdapter(options)

            adapter?.onItemClickListener = { trip ->
                val bundle = Bundle()
                bundle.putString("tripId", trip.id)

                val fragment = ViewTripFragment()
                fragment.arguments = bundle


                parentFragmentManager.commit {
                    replace(R.id.frame_container, fragment )
                    addToBackStack(null)
                }
            }

            recyclerView.adapter = adapter
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}
