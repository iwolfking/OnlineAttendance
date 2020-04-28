package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_teacher_create_desks.*


class TeacherCreateDesks : AppCompatActivity() {

    /**
     * Variables that are required to use Firebase, perform Firebase calls in this activity.
     */
    private lateinit var auth: FirebaseAuth
    var user = FirebaseAuth.getInstance().currentUser
    var db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = null

    // Variables for the columns/rows of seats in the class. Set as string, convert to Int when needed.
    var numColumns = "0"
    var numRows = "0"
    private var sessionsToDate = "0"
    var seatTaken: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_create_desks)

        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        updateUI()

        // Submits the attendance, but updates the sessions for the course by 1.
        // TODO: Create prompt, if they want to submit attendance for all students.
        submitAttendance.setOnClickListener {
            updateNumberOfSessions()
        }

        // Allows the teacher to change the number of rows/columns of chairs in the classroom.
        editSeatsButton.setOnClickListener {
            val setColumnsAndRowsForSeatsIntent = Intent(this, SetColumnsAndRowsForSeats::class.java)
            startActivity(setColumnsAndRowsForSeatsIntent)
        }

        // Refreshes the activity, in case they changed the number of chairs.
        testStudentButton.setOnClickListener {
            finish();
            startActivity(intent);
        }
    }

    private fun updateUI() {
        if (user != null) {

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            val uid = user!!.uid
            getTeacherName()
            getProfilePicture()
            getSeats()
        }
    }

    // Method used to get the teacher's name from the Firestore database, then it displays their name.
    private fun getTeacherName() {
        val uid = user!!.uid

        db.collection("teachers")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            println("Teacher Name: " + document["firstName"] + " " + document["lastName"])
                            teachersNameCreateDesksTextView.text = (document["firstName"].toString() + " " + document["lastName"].toString())
                        } else {
                            Log.d(
                                "Document",
                                document.id + " => " + document.data
                            )
                        }

                    }
                } else {
                    Log.w("Empty", "Error getting documents.", task.exception)
                }
            }
    }

    // Method used to get the teacher's profile picture, and display it.
    private fun getProfilePicture() {
        val uid = user!!.uid

        //Download file in Memory
        val profilePictureRef = mStorageRef?.child("pics/$uid")
        // Not truly a megabyte, had to increase for larger photo downloads.
        val ONE_MEGABYTE = 1024 * 1024 * 50.toLong()
        profilePictureRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
            teacherProfilePictureCreateDesksImageView.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
        }?.addOnFailureListener {
            // Doesn't display photo, may occur for multiple reasons.
            println("Error" + uid + " doesn't exist.")
        }
    }

    // Method used to get the number of seats that were set in SetColumnsAndRowsForSeats.
    private fun getSeats() {
        val uid = user!!.uid

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            numColumns = document["columns"] as String
                            numRows = document["rows"] as String
                            // Create Desks on the page, now that we know how many to make.
                            createDesks(numColumns.toInt(), numRows.toInt())
                        } else {
                            Log.d(
                                "Document",
                                document.id + " => " + document.data
                            )
                        }

                    }
                } else {
                    Log.w("Empty", "Error getting documents.", task.exception)
                }
            }
    }

    // Gets the total number of sessions this course has had so far. Need for updating when submit.
    private fun getNumberOfSessions() {

        val uid = user!!.uid

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            // On this case, then we are on the class for the teacher
                            sessionsToDate = document["sessions"].toString()
                        } else {
                            Log.d(
                                "Document",
                                document.id + " => " + document.data
                            )
                        }

                    }
                } else {
                    Log.w("Empty", "Error getting documents.", task.exception)
                }
            }
    }

    // If the teacher submits attendance, then it will increase the sessions to date by 1.
    private fun updateNumberOfSessions() {

        val uid = user!!.uid

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            // On this case, then we are on the class for the teacher
                            getNumberOfSessions()
                            var courseRef = db.collection("courses").document(document.id.toString())
                            sessionsToDate = (sessionsToDate.toInt() + 1).toString()
                            courseRef.update("sessions", sessionsToDate)
                        } else {
                            Log.d(
                                "Document",
                                document.id + " => " + document.data
                            )
                        }

                    }
                } else {
                    Log.w("Empty", "Error getting documents.", task.exception)
                }
            }
    }


    private fun createDesks(columns: Int, rows: Int) {

        /**
         * Reference material found on how to create a variable number of rows/columns for a
         * GridLayout: https://stackoverflow.com/questions/35692984/programmatically-adding-textview-to-grid-layout-alignment-not-proper
         * used in StudentChooseDesk.kt & TeacherCreateDesks.kt
         */

        // Sets number of columns in the grid view.
        seatsGridLayout.columnCount = columns
        seatsGridLayout.rowCount = rows


        // Number of total seats in the class.
        var totalSeats = columns * rows

        // Loop used to generate the seats in the layout.
        for(i in 1..totalSeats) {
            val seat = Button(this)
            seat.text = "Seat #: " + i.toString()
            db.collection("seats")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {

                            if (document["seat"] == i.toString()) {
                                // On this case, then we know the seat is taken.
                                if (document["student"].toString() != "null") {
                                    println(document["student"].toString())
                                    // change color to red for button
                                    seatTaken = true
                                    var red = Color.parseColor("#FF0000")
                                    seat.setBackgroundColor(red)
                                    seat.setOnClickListener {
                                        val studentProfileTeacherViewIntent = Intent(this, StudentProfileTeacherView::class.java)
                                        studentProfileTeacherViewIntent.putExtra("studentSeat", i.toString())
                                        startActivity(studentProfileTeacherViewIntent)
                                    }
                                } else {
                                    // let it be green by default
                                    seatTaken = false
                                    var green = Color.parseColor("#008000")
                                    seat.setBackgroundColor(green)
                                }

                            } else {
                                Log.d(
                                    "Document",
                                    document.id + " => " + document.data
                                )
                                //seatTaken = false
                            }

                        }
                    } else {
                        Log.w("Empty", "Error getting documents.", task.exception)
                    }
                }

            seatsGridLayout.addView(seat)

        }
    }

}
