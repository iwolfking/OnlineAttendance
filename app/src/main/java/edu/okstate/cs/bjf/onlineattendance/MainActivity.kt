package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_profile.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginButton.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        createProfileButton.setOnClickListener {
            val createProfileIntent = Intent(this, NewProfileActivity::class.java)
            startActivity(createProfileIntent)
        }
    }
}
