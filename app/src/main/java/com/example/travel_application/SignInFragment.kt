package com.example.travel_application

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth


class SignInFragment : Fragment() {


    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()

        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        emailEditText = view.findViewById(R.id.email)
        passwordEditText = view.findViewById(R.id.passw)
        loginButton = view.findViewById(R.id.logbutton)


        loginButton.setOnClickListener {
            performLogin()

            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)

        }

        val signUpTextView: TextView = view.findViewById(R.id.signup)
        signUpTextView.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, SignUpFragment())
                addToBackStack(null)
            }
        }

        return view
    }
    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Wszystkie pola są wymagane", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Zalogowano pomyślnie", Toast.LENGTH_SHORT).show()
                } else {

                    Toast.makeText(requireContext(), "Logowanie nie powiodło się. Zły login lub hasło.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Logowanie nie powiodło się. Spróbuj ponownie.", Toast.LENGTH_SHORT).show()
            }
    }

}