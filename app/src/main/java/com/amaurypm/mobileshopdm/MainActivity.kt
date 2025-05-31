package com.amaurypm.mobileshopdm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.lifecycle.lifecycleScope
import com.amaurypm.mobileshopdm.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //Para la autenticación
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Instanciamos el firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Instanciamos el credential manager
        credentialManager = CredentialManager.create(this)

        //Ponemos en los textviews correspondientes la info del usuario
        binding.tvUsuario.text = firebaseAuth.currentUser?.displayName
        binding.tvMail.text = firebaseAuth.currentUser?.email

        //Cargamos la imagen de perfil
        Glide.with(this)
            .load(firebaseAuth.currentUser?.photoUrl)
            .into(binding.ivProfile)

        binding.ivLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea cerrar sesión para el usuario ${firebaseAuth.currentUser?.displayName}?")
                .setPositiveButton("Aceptar") { _,_ ->
                    //Cerrar sesión
                    firebaseAuth.signOut()  //Cerramos sesión en firebase

                    val request = ClearCredentialStateRequest()

                    lifecycleScope.launch {
                        try{
                            //Desasociamos la app con nuestra cuenta Google mediante el credential manager
                            credentialManager.clearCredentialState(request)

                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()

                            Toast.makeText(
                                this@MainActivity,
                                "Sesión finalizada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }catch (e: ClearCredentialException){
                            //Manejamos el error
                            e.printStackTrace()
                        }
                    }
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

    }
}