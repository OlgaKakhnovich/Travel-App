package com.example.travel_application

import android.os.Binder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        return binding.root
    }
    companion object {

    }
}