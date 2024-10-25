package com.example.travel_application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travel_application.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentSignUpBinding
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()


        binding.btnSignup.setOnClickListener {
            val name = binding.nameSignup.text.toString()
            val surname = binding.surnameSigup.text.toString()
            val username = binding.usernameSignup.text.toString()
            val email = binding.emailSignup.text.toString()
            val password = binding.passwordSignup.text.toString()
            val password2 = binding.password2Signup.text.toString()

            if (name.isNotEmpty() && surname.isNotEmpty() && username.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty() && password == password2) {
                registerUser(email, password, name, surname, username)
            } else {
                Toast.makeText(activity, "Proszę wypełnić wszystkie pola poprawnie.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun registerUser(email: String, password: String, name: String, surname: String, username: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val userId = firebaseAuth.currentUser?.uid
                    val userMap = hashMapOf(
                        "name" to name,
                        "surname" to surname,
                        "username" to username,
                        "email" to email
                    )


                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(activity, "Rejestracja udana", Toast.LENGTH_SHORT).show()
                                //przejść do innego fragmentu
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(activity, "Błąd podczas zapisywania danych: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(activity, "Błąd rejestracji. Spróbuj ponownie.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
