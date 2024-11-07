package com.example.travel_application

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_application.databinding.FragmentEditProfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.commit
import com.google.firebase.firestore.SetOptions

class EditProfilFragment : Fragment() {

    private lateinit var binding: FragmentEditProfilBinding
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private  var imageUri: Uri?=null
    private var pickImg = 100
     //private val storage = Firebase.storage
     private lateinit var firebaseRepository: FirebaseRepository
    //lateinit var imageView: imageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfilBinding.inflate(inflater,container,false)
        firebaseRepository = FirebaseRepository(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()
        //loadImageFromFirebase
        //funkcja dla zmiany zdjecia

        firebaseRepository.getName(
            onSuccess = {
                name->binding.name.setText(name)
            },
            onFailure = {
                binding.name.setText("Edytuj imię")
            })

        firebaseRepository.getSurname(
            onSuccess = {surname->
                binding.surname.setText(surname)
            },
            onFailure = {
                binding.surname.setText("Edytuj nazwisko")
            }
        )

        firebaseRepository.getLocation(
            onSuccess = { location ->
                binding.location.setText(location)
            },
            onFailure = {binding.location.setText("Edytuj lokalizacje")}
        )


        //zdjecie, haslo

        binding.confirmButton.setOnClickListener{
            val name = binding.name.text.toString().trim()
            val lastname = binding.surname.text.toString().trim()
            val location = binding.location.text.toString().trim()
            updateUser(name, lastname, location.ifEmpty { null })

            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }


    private fun updateUser(name: String, lastname: String, location: String?){
        if(name.isNotEmpty() && lastname.isNotEmpty()){
/*
            if(imageUri!=null){
                //img dodamy pozniej
            }
            */
            val userData = mutableMapOf<String, Any>(
                "name" to name,
                "lastname" to lastname,
            )
            val userRef = db.collection("user").document(userId)
            userRef.get().addOnSuccessListener { document ->
                if(document.exists()){
                    val locationExists = document.contains("location")

                    if(!location.isNullOrEmpty()){
                        if(locationExists){
                            userRef.update( "location", location)
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }else{
                            userRef.set(mapOf("location" to location), SetOptions.merge())
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }
                    }

                    userRef.update(userData)
                        .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                        .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }

                }else{
                    Log.w(TAG, "User document does not exist")
                }
            }.addOnFailureListener { e->
                Log.w(TAG, "Failed to fetch user document", e)
            }
        }
        else{
            Toast.makeText(requireContext(), "Wypełnij pola", Toast.LENGTH_SHORT).show()
        }
    }

//dodac funkcje po zmianie zdjecia
    companion object {
       fun newInstance(param1: String, param2: String) =
           EditProfilFragment().apply {
               arguments = Bundle().apply {  }
           }
    }
}