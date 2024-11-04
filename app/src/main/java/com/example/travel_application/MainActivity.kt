package com.example.travel_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sprawdź, czy fragment nie został już dodany, aby uniknąć duplikacji po odtworzeniu aktywności
        if (savedInstanceState == null) {
            // Utwórz nową instancję fragmentu
            val firstPageFragment = FirstPageFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, firstPageFragment)
            transaction.commit()
        }
    }
}