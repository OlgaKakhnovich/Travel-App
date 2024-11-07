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
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.commit

class EditProfilFragment : Fragment() {

    private lateinit var binding: FragmentEditProfilBinding
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private  var imageUri: Uri?=null
    private var pickImg = 100
     // private val storage = Firebase.storage
     private lateinit var firebaseRepository: FirebaseRepository
    //lateinit var imageView: imageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        //zmieniamy lakalizacje, zdjecie, haslo

        binding.confirmButton.setOnClickListener{
            val name = binding.name.text.toString()
            val lastname = binding.surname.text.toString()
            updateUser(name, lastname)

            parentFragmentManager.commit{
                replace(R.id.fragment_container, ProfilFragment())
                addToBackStack(null)
            }

        }

        return binding.root
    }


    private fun updateUser(name: String, lastname: String){
        if(name.isNotEmpty() && lastname.isNotEmpty()){
            if(imageUri!=null){
                //img dodamy pozniej
            }
            db.collection(userId).document("user")
                .update(
                    mapOf(
                        "name" to name,
                        "lastname" to lastname,
                    )
                ).addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
        }
        else{
            Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
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