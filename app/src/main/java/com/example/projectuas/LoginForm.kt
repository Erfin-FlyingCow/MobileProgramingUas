package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginForm : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var btnLogin: TextView
    private lateinit var mauth: FirebaseAuth
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val RC_SIGN_IN: Int = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_form)

        // Setting insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initializing login button and progress bar
        btnLogin = findViewById(R.id.googlelogin)
        progressBar = findViewById(R.id.progress)

        // Initializing FirebaseAuth
        mauth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize GoogleApiClient
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this) {
                Toast.makeText(applicationContext, "Koneksi akun google gagal", Toast.LENGTH_SHORT).show()
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        // Set click listener for the login button
        btnLogin.setOnClickListener {
            loginAkunGoogle()
        }
    }

    private fun loginAkunGoogle() {
        progressBar.visibility = ProgressBar.VISIBLE
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }
            if (result?.isSuccess == true) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
            } else {
                Toast.makeText(applicationContext, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.INVISIBLE
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mauth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = ProgressBar.GONE
                if (task.isSuccessful) {
                    // Sign-in succeeded
                    val user = mauth.currentUser
                    Intent(this,MainActivity::class.java).also {
                        startActivity(it)
                    }
                } else {
                    // Sign-in failed
                    Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
