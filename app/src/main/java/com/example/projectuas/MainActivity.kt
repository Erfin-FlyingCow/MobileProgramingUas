package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

import com.google.firebase.auth.FirebaseAuth

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {

    private lateinit var mauth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        replaceFragmaent(HomeFragment())
        mauth = FirebaseAuth.getInstance()

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when  (item.itemId) {
            R.id.logout -> {
                logout()
                true
            }
            R.id.tambahTempatFavorite -> {
                replaceFragmaent(FormLokasi())
                true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private  fun replaceFragmaent(homeFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container,homeFragment)
        fragmentTransaction.commit()
    }

    private fun logout() {
        mauth.signOut()
        if (mauth.currentUser == null) {
            login_form()
        }
    }
     fun login_form (){
        Intent(this, LoginForm::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }


}