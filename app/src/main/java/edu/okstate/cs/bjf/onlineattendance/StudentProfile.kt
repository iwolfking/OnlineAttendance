package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_student_profile.*

class StudentProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        submitCourseEditText.setOnClickListener {
            val studentChooseDeskIntent = Intent(this, StudentChooseDesk::class.java)
            startActivity(studentChooseDeskIntent)
        }
    }
}
