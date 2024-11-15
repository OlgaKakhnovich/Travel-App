package com.example.travel_application

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Binder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.commit
import com.example.travel_application.databinding.FragmentProfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


class ProfilFragment : Fragment() {

    private lateinit var binding: FragmentProfilBinding
    private  val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var myTrips: Button
    private lateinit var addTrip: Button
    private lateinit var addWish: Button


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentProfilBinding.inflate(inflater,container, false)
        firebaseRepository =FirebaseRepository(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()

        firebaseRepository.getAbout(
            onSuccess = { about ->
                binding.about.text = about
            },
            onFailure = { binding.about.text = "" }
        )

        firebaseRepository.getUserName(
            onSuccess = { userName ->
                binding.userName.text = userName
            },
            onFailure = { binding.about.text = "" }
        )

        firebaseRepository.getLocation(
            onSuccess = {userLocation ->
                binding.userCity.text = userLocation
            },
            onFailure = {binding.userCity.text = ""}
        )

/*  z dolnym menu
        addTrip = binding.addPlace
        addTrip.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.frame_container, AddTripFragment())
                addToBackStack(null)
            }
        }*/


        //bez menu

        addTrip = binding.addPlace
        addTrip.setOnClickListener {

            val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottom_navigation)
            bottomNavigationView.visibility = View.GONE

            parentFragmentManager.commit {
                replace(R.id.frame_container, AddTripFragment())
                addToBackStack(null)
            }
        }

        myTrips=binding.tripList
        myTrips.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.frame_container, ListTravelFragment() )
                addToBackStack(null)
            }
        }

        addWish = binding.addWish
        addWish.setOnClickListener {
            showAddWishDialog()
        }

        return binding.root
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddWishDialog() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_add_wish, null)

        val enterCountry = dialogView.findViewById<EditText>(R.id.enterCountryWish)
        val enterCity = dialogView.findViewById<EditText>(R.id.enterCityWish)
        val acceptBtn = dialogView.findViewById<Button>(R.id.acceptBtn)
        val cancelBtn = dialogView.findViewById<TextView>(R.id.cancelBtn)


        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()

        acceptBtn.setOnClickListener {
            val country = enterCountry.text.toString()
            val city = enterCity.text.toString()

            if(country.isNotEmpty() && city.isNotEmpty()){
                saveDataToBase(country,city)
                alertDialog.dismiss()
            }else{
                Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }

        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }

       alertDialog.show()
    }

    private fun saveDataToBase(country: String, city: String) {
        val wishRef = db.collection("user").document(userId).collection("wish")
        val wishMap = hashMapOf(
            "country" to country,
            "city" to city
        )

        wishRef.document().set(wishMap).addOnFailureListener {
            Toast.makeText(
                requireContext(),
                "Twoje życzenie zostało dodano",
                Toast.LENGTH_SHORT
            ).show()

        }

    }


    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNavigationView.visibility = View.VISIBLE
    }

    companion object {

    }
}