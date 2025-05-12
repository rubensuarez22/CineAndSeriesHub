package com.example.cineyhub

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button // Cambiado de MaterialButton
import android.widget.EditText // Cambiado de TextInputEditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener // Funciona con EditText también
import com.example.cineyhub.dataAplication.Companion.prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Registry : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry) // Esto debería funcionar después de cambiar el XML

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referencias al XML actualizadas
        // Ya no necesitas tilFullName, tilEmailRegister, tilPasswordRegister si los quitaste del XML
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmailRegister = findViewById<EditText>(R.id.etEmailRegister)
        val etPasswordRegister = findViewById<EditText>(R.id.etPasswordRegister)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount) // Cambiado
        val tvGoLogin = findViewById<TextView>(R.id.tvGoLogin)
        val pbLoadingRegister = findViewById<ProgressBar>(R.id.pbLoadingRegister)

        // Limpiar errores al editar - setError(null) funciona en EditText
        etFullName.addTextChangedListener { etFullName.error = null }
        etEmailRegister.addTextChangedListener { etEmailRegister.error = null }
        etPasswordRegister.addTextChangedListener { etPasswordRegister.error = null }

        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnCreateAccount.setOnClickListener {
            // Limpiar errores previos
            etFullName.error = null
            etEmailRegister.error = null
            etPasswordRegister.error = null

            val fullName = etFullName.text.toString().trim()
            val email = etEmailRegister.text.toString().trim()
            val password = etPasswordRegister.text.toString()

            if (fullName.isEmpty()) {
                etFullName.error = "Ingresa tu nombre" // Cambiado de tilFullName.error
                etFullName.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailRegister.error = "Correo inválido" // Cambiado de tilEmailRegister.error
                etEmailRegister.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPasswordRegister.error = "Mínimo 6 caracteres" // Cambiado de tilPasswordRegister.error
                etPasswordRegister.requestFocus()
                return@setOnClickListener
            }

            btnCreateAccount.isEnabled = false
            pbLoadingRegister.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    btnCreateAccount.isEnabled = true
                    pbLoadingRegister.visibility = View.GONE

                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            val userMap = hashMapOf(
                                "name" to fullName,
                                "rol" to "0"
                            )
                            db.collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    prefs.saveNombre(fullName)
                                    prefs.saveRol("0")
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Error inesperado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Error de registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}