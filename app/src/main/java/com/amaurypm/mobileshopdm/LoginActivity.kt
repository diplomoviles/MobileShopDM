package com.amaurypm.mobileshopdm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.amaurypm.mobileshopdm.databinding.ActivityLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //Para el sign in con Google:
    private lateinit var googleIdOption: GetGoogleIdOption //Para el bottom sheet
    private lateinit var signInWithGoogleOption: GetSignInWithGoogleOption//Para el flujo de un botón
    private lateinit var credentialManager: CredentialManager

    //Para firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Iniciamos la instancia a firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Iniciamos la instancia al credential manager
        credentialManager = CredentialManager.create(this)

        //Generamos el nonce de 32 caracteres
        val nonce = NonceUtils.generateNonce(32)

        //Para mostrar las cuentas con bottom sheet
        googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(nonce)
            .build()

        //Para el flujo de un botón
        signInWithGoogleOption = GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID)
            .setNonce(nonce)
            .build()

        binding.btnSignInGoogle.setOnClickListener {
            //Si usamos el bottom sheet
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
                }catch (e: Exception){
                    if(e.message.equals("No credential available")){
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("Error")
                            .setMessage("No hay cuentas asociadas. Por favor asocie una en la configuración del dispositivo")
                            .setNeutralButton("Aceptar") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }
            }
        }
    }
}