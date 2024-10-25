package com.example.travel_application

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.travel_application.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentSignInBinding
    val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSignInBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSignin.setOnClickListener{
            val email = binding.emailSignin.text.toString()
            val password = binding.passwordSignin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInUser(email, password)
            } else {
                Toast.makeText(activity, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }

        }

        return binding.root
    }




    private fun signInUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "Logowanie udane", Toast.LENGTH_SHORT).show()
             //   val intent = Intent(activity, MainActivity::class.java)
                //startActivity(intent)
            } else {
                Toast.makeText(activity, "Błąd logowania. Spróbuj ponownie.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}