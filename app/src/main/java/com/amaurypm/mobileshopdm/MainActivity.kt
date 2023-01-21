package com.amaurypm.mobileshopdm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.amaurypm.mobileshopdm.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    //Para firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Instanciando el objeto de firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Obteniendo los datos del usuario que se autenticó
        binding.tvUsuario.text = firebaseAuth.currentUser?.displayName
        binding.tvMail.text = firebaseAuth.currentUser?.email


        binding.ivLogout.setOnClickListener {
            //Cerramos sesión en Firebase Auth
            firebaseAuth.signOut()

            //Desasociamos la app con la cuenta Google

            GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                .signOut().addOnSuccessListener {
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "No se pudo cerrar sesión", Toast.LENGTH_SHORT).show()
                }

        }

    }
}