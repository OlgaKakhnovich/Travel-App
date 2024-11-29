package com.example.travel_application

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travel_application.databinding.FragmentEditProfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.hbb20.CountryCodePicker

class EditProfilFragment : Fragment() {

    private lateinit var binding: FragmentEditProfilBinding
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
     private lateinit var firebaseRepository: FirebaseRepository
     private lateinit var countryCodePicker: CountryCodePicker


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfilBinding.inflate(inflater,container,false)
        firebaseRepository = FirebaseRepository(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()

        countryCodePicker = binding.ccp


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

        firebaseRepository.getAbout(
            onSuccess = { about ->
                binding.about.setText(about)
            },
            onFailure = {binding.about.setText("O mnie")}
        )


        firebaseRepository.getUserName(
            onSuccess = { userName ->
                binding.username.setText(userName)
            },
            onFailure = {binding.about.setText("Imię użytkownika")}
        )

        firebaseRepository.getCountryCode(
            onSuccess = { countryCode ->
            countryCodePicker.setCountryForNameCode(countryCode)
            },
            onFailure = {}
        )



        binding.confirmButton.setOnClickListener{
            updateUser()

            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }






    private fun updateUser(){
        val name = binding.name.text.toString().trim()
        val lastname = binding.surname.text.toString().trim()
        val username = binding.username.text.toString().trim()

        val selectedCountryCodeInt = countryCodePicker.selectedCountryCodeAsInt
        val selectedCountryCodeName = countryCodePicker.selectedCountryNameCode
        val about = binding.about.text.toString().trim().ifEmpty { "" }


        if(name.isNotEmpty() && lastname.isNotEmpty()){
            val userData = mutableMapOf<String, Any>(
                "name" to name,
                "lastname" to lastname,
                "username" to username,
            )
            val userRef = db.collection("user").document(userId)
            userRef.get().addOnSuccessListener { document ->
                if(document.exists()){

                    val countryExists = document.contains("country")
                    val aboutExists = document.contains("about")

                   if(selectedCountryCodeInt!=null){
                       val countryData = mutableMapOf<String, Any>(
                           "countryCode" to selectedCountryCodeName,
                       )
                        if(countryExists){
                            userRef.update( countryData)
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }else{
                            userRef.set(countryData)
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }
                    }

                    if(about!=null){
                        if(aboutExists){
                            userRef.update( "about", about)
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }else{
                            userRef.set(mapOf("about" to about), SetOptions.merge())
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