package com.amaurypm.mobileshopdm

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.amaurypm.mobileshopdm.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {

    //Para autenticarnos con Google
    lateinit var gso: GoogleSignInOptions
    lateinit var signInClient: GoogleSignInClient

    //Para el servicio de autenticación con Firebase
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Instanciando firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Generando las google sign in options, pidiendo el correo del usuario
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1019217216850-no0tpes4v0mrn1505khm6nch51rueeem.apps.googleusercontent.com")
            .requestEmail()
            .build()

        //Instanciando el Google Sign in client
        signInClient = GoogleSignIn.getClient(this, gso)

        //Para verificar si ya había accedido previamente
        val signInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        //Si la variable anterior no regresa nulo, entonces el usuario ya había ingresado previamente

        if(signInAccount!=null && firebaseAuth.currentUser!=null){
            Toast.makeText(this, "Usuario ya registrado", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //Aquí procesamos la respuesta del intent del sign in de Google
        var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){

                //En una tarea (task) buscamos obtener los datos del proceso del sign in
                val signInTask: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                try{

                    //Establecemos un elemento GoogleSignInAccount para obtener finalmente los datos, manejando su excepción
                    val signInAccount = signInTask.getResult(ApiException::class.java)

                    //Agregamos los datos de ingreso a FirebaseAuth

                    //Paso 1: Obtenemos la credencial de la cuenta de Google para usarla en Firebase
                    val authCredential = GoogleAuthProvider.getCredential(signInAccount.idToken, null)

                    //Paso 2: Usamos esa credencial para loggearnos en Firebase Auth también

                    firebaseAuth.signInWithCredential(authCredential).addOnSuccessListener {
                        //Si la autenticación con Firebase se realizó correctamente
                        Toast.makeText(this, "Tu cuenta de Google se ha conectado a la aplicación", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        //Si hubo algún error al loggearnos con Firebase Auth
                        Toast.makeText(this, "No se pudo completar el registro", Toast.LENGTH_SHORT).show()
                    }
                }catch (e: ApiException){
                    e.printStackTrace()
                }


            }
        }

        //Programando el botón para autenticarnos con Google
        binding.btnEntrarGoogle.setOnClickListener {
            val signIntent = signInClient.signInIntent
            resultLauncher.launch(signIntent)
        }
    }
}