package com.example.travel_application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class FirstPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_first_page, container, false)



        val loginButton: Button = view.findViewById(R.id.loginbutton)
        val signUpText: TextView = view.findViewById(R.id.nie_masz_jeszcze_konta__zarejestruj_si__teraz_)


        loginButton.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, SignInFragment())
                addToBackStack(null)
            }
        }


        signUpText.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, SignUpFragment())
                addToBackStack(null)
            }
        }

        return view
    }
}
