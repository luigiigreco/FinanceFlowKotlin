package com.example.financeflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        // Controllo preliminare per email e password
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email o password non possono essere vuoti", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login riuscito, reindirizza alla MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Mostra errore
                    Toast.makeText(this, "Errore di login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun registerUser(email: String, password: String) {
        // Controllo preliminare per email e password
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email o password non possono essere vuoti", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registrazione riuscita, reindirizza alla MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Mostra errore
                    Toast.makeText(this, "Errore di registrazione: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

}
