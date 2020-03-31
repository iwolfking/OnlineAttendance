package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_profile.*

class NewProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_profile)

        newTeacherButton.setOnClickListener {
            val newTeacherProfileIntent = Intent(this, CreateTeacherProfileActivity::class.java)
            startActivity(newTeacherProfileIntent)
        }

        newStudentButton.setOnClickListener {
            val newStudentProfileIntent = Intent(this, CreateStudentProfileActivity::class.java)
            startActivity(newStudentProfileIntent)
        }
    }
}
