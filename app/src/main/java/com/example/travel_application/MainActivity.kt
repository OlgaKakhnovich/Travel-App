package com.example.travel_application

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit



class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSignIn = findViewById<Button>(R.id.btn_signin)
        val btnSignUp = findViewById<Button>(R.id.btn_signup)

        btnSignIn.setOnClickListener {

                val intent = Intent(this, SignInFragment::class.java)
                startActivity(intent)

        }

        btnSignUp.setOnClickListener {

                val intent = Intent(this, SignUpFragment::class.java)
                startActivity(intent)

        }


    }
}