package com.example.travel_application

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.bottom_news->{
                    replaceFragment(NewsFragment())
                    true
                }
                R.id.bottom_map->{
                    replaceFragment(MapFragment())
                    true
                }
                R.id.bottom_profile->{
                    replaceFragment(ProfilFragment())
                    true
                }

                R.id.bottom_wish->{
                    replaceFragment(WishListFragment())
                    true
                }
                R.id.bottom_settings->{
                    replaceFragment(EditProfilFragment())
                    true
                }
                else->false
            }
        }
        replaceFragment(NewsFragment())
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}