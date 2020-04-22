package edu.okstate.cs.bjf.onlineattendance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class StudentChooseDesk : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_choose_desk)
    }

    /** TODO(4): Implement a Kotlin Class/structure to allow a student to choose
     *           from desks that get loaded from the database;
     *           then submit this to the database to reflect on the teacher's side
     *           which desks have a student in them. Create a grid layout
     *           of desks for the student to see.
     */

    /** TODO(5): Create percentile method, to store the number of times the student
     *           has attended the course, for the student & teacher to be able to see.
     */
}
