package com.example.cineyhub

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.cineyhub.dataAplication.Companion.prefs
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // Firebase
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        checkUserValues()

        // Vistas

        val etEmail = findViewById<TextView>(R.id.etEmail)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvGoRegister  = findViewById<TextView>(R.id.tvGoRegister)
        val pbLoading     = findViewById<ProgressBar>(R.id.pbLoading)


        // Login
        btnLogin.setOnClickListener {
            // Limpia errores previos

            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // Validaciones
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Deshabilita botón y muestra loader
            btnLogin.isEnabled = false
            pbLoading.visibility = View.VISIBLE

            // Intento de login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // Siempre re-habilita botón y oculta loader
                    btnLogin.isEnabled = true
                    pbLoading.visibility = View.GONE

                    if (task.isSuccessful) {
                        auth.currentUser?.uid?.let { uid ->
                            loadUserData(uid) { data ->
                                if (data != null) {
                                    prefs.saveNombre(data["name"] as? String ?: "")
                                    prefs.saveRol(data["rol"] as? String ?: "")

                                    when (data["rol"] as? String) {
                                        "0" -> goToUserMain()
                                        "1" -> goToAdminMain()
                                        else -> Toast.makeText(this, "Rol no válido", Toast.LENGTH_SHORT).show()
                                    }
                                    finish()
                                } else {
                                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Ir a Registro
        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, Registry::class.java))
            finish()
        }
    }

    private fun checkUserValues() {
        val nombre = prefs.getNombre()
        val rol    = prefs.getRol()
        if (!nombre.isNullOrEmpty() && !rol.isNullOrEmpty()) {
            when (rol) {
                "0" -> goToUserMain()
                "1" -> goToAdminMain()
                else -> Toast.makeText(this, "Rol no válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData(userId: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                onResult(if (doc.exists()) doc.data else null)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
    }

    private fun goToUserMain() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private fun goToAdminMain() {
        startActivity(Intent(this, HomeActivity::class.java))
    }
}
