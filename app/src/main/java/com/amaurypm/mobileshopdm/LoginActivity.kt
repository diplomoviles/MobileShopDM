package com.amaurypm.mobileshopdm


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.lifecycleScope
import com.amaurypm.mobileshopdm.databinding.ActivityLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //Para el sign in con Google:
    private lateinit var googleIdOption: GetGoogleIdOption
    private lateinit var signInWithGoogleOption: GetSignInWithGoogleOption
    private lateinit var credentialManager: CredentialManager

    //Para el servicio de autenticación con Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Instanciamos el firebaseauth
        firebaseAuth = FirebaseAuth.getInstance()

        //Iniciamos la instancia al credential manager:
        credentialManager = CredentialManager.create(this)

        //Generamos un nonce de 32 caracteres
        val nonce = NonceUtils.generateNonce(32)

        //Para mostrar las cuentas con un bottom sheet
        googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .setNonce(nonce)
        .build()


        //Para usar el flujo de un botón
        signInWithGoogleOption = GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id))
            .setNonce(nonce)
        .build()

        if(firebaseAuth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnSignInGoogle.setOnClickListener {

            //Ponemos la petición al sign in
            //Si usamos un bottom sheet:
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            //Si usamos el flujo de un botón
            /*val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()*/

            lifecycleScope.launch {
                try{
                    val result = credentialManager.getCredential(
                        this@LoginActivity,
                        request
                    )
                    handleSignIn(result)
                }catch (e: Exception){
                    //Manejamos el error
                    e.printStackTrace()
                }
            }


        }

    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Manejamos la credencial obtenida
        when (val credential = result.credential) {
            is CustomCredential -> {
                //Verificamos si el resultado es un token de una credencial google
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Usamos el googleIdTokenCredential y lo extraemos para autenticarnos en nuestro server

                        //Obtenemos el token de google
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        //usamos el token para sacar la credencial de autenticación
                        val authCredential =
                            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                        firebaseAuth.signInWithCredential(authCredential)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Tu cuenta Google se ha conectado a la app", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "No se pudo completar el registro", Toast.LENGTH_SHORT).show()
                            }

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("TAG", "Respuesta de token id de Google inválida", e)  //el “e” registra la traza completa del error en el log
                    }
                } else {
                    // Capturamos el error de una credencial no reconocida
                    Log.e("TAG", "Credencial no reconocida")
                }
            }

            else -> {
                // Capturamos el error de una credencial no reconocida
                Log.e("TAG", "Credencial no reconocida")
            }
        }
    }

}