package com.mutia.deteksistrawberry

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // default halaman
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, home())
            .commit()

        bottomNav.setOnItemSelectedListener {

            val fragment = when (it.itemId) {
                R.id.nav_home -> home()
                R.id.nav_info -> info()
                R.id.nav_history -> History()
                R.id.nav_about -> about()
                else -> home()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit()

            true
        }
    }
}