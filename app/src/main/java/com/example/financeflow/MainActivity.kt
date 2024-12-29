package com.example.financeflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.financeflow.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Imposta la bottom navigation con il NavController
        navView.setupWithNavController(navController)

        // Configura l'ImageView come pulsante per il logout
        val logoImageView: ImageView = findViewById(R.id.logoImageView)
        logoImageView.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // Mostra il dialogo di conferma per il logout
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Conferma Logout")
        builder.setMessage("Sei sicuro di voler uscire?")
        builder.setPositiveButton("Sì") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("Annulla", null)
        builder.show()
    }

    // Metodo per eseguire il logout
    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

}
