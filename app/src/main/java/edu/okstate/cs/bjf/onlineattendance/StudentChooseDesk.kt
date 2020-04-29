package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_student_choose_desk.*
import kotlinx.android.synthetic.main.activity_student_profile_teacher_view.*
import kotlinx.android.synthetic.main.activity_teacher_create_desks.*

class StudentChooseDesk : AppCompatActivity() {

    // Sent through Intent, this is the seat that was selected by the student.
    var courseName = "CS4153"

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
    private var sessionAmountTV = this.findViewById<TextView>(R.id.attendanceAmount)
    var seatTaken: Boolean = false

    // Used to determine, if a student has already chosen a seat.
    private var studentHasPickedSeat = false
    private var studentsAttendanceToDate = "0"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_choose_desk)

        courseName = intent.getStringExtra("courseName")

        // Initialize Firebase variables, and get the number of seats to know how to generate them.
        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        getStudentName()
        getProfilePicture()
        getCourseSessions()
        getAttendance()
        getSeats()

    }

    // Gets the student's name, and is displayed by getStudent()
    private fun getStudentName() {
        val uid = user!!.uid

        db.collection("students")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["student"] == uid) {
                            println("Student Name: " + document["firstName"] + " " + document["lastName"])
                            studentNameInChooseDeskActivity.text = (document["firstName"].toString() + " " + document["lastName"].toString())
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

    // Gets the student's profile picture, and is displayed by getStudent()
    private fun getProfilePicture() {
        val uid = user!!.uid
        // Create a storage reference from our app


        /*In this case we'll use this kind of reference*/
        //Download file in Memory
        val profilePictureRef = mStorageRef?.child("pics/$uid")

        val ONE_MEGABYTE = 1024 * 1024 * 50.toLong()
        profilePictureRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
            // Data for "images/island.jpg" is returns, use this as needed

            studentProfilePictureInChooseDeskActivity.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
        }?.addOnFailureListener {
            // Handle any errors
            println("ERROR")
            println("Error" + uid + " doesn't exist.")
        }
    }

    // Used to find the times the student has attended the course.
    private fun getAttendance() {

        val uid = user!!.uid

        db.collection("attendance")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["student"] == uid) {
                            // On this case, then we are on the class for the teacher
                            studentsAttendanceToDate = document["attendance"].toString()
                            var percentAttended: Double = (studentsAttendanceToDate.toDouble() / sessionsToDate.toDouble()) * 100
                            studentAttendanceInChooseDeskActivity.text = percentAttended.toString() + "%"
                            //Set number of attended courses textview
                            val sessionString : String = "Attendance Amount: " + studentsAttendanceToDate.toString()
                            sessionAmountTV.text = sessionString
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

    // Gets the value of the total number of sessions the class has had to date.
    private fun getCourseSessions() {
        val course = courseName

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["course"] == course) {
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

    /**
     * Function that gets the number of seats that are in this class, that was set by the teacher.
     * It gets the number of columns/rows, and calculates the number of total seats.
     */

    private fun getSeats() {

        val course = courseName

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["course"] == course) {
                            numColumns = document["columns"] as String
                            numRows = document["rows"] as String
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

    private fun createDesks(columns: Int, rows: Int) {

        val uid = user!!.uid

        /**
         * Reference material found on how to create a variable number of rows/columns for a
         * GridLayout: https://stackoverflow.com/questions/35692984/programmatically-adding-textview-to-grid-layout-alignment-not-proper
         * used in StudentChooseDesk.kt & TeacherCreateDesks.kt
         */

        // Sets number of columns in the grid view.
        studentChairViewGridLayout.columnCount = columns
        studentChairViewGridLayout.rowCount = rows


        // Number of total seats in the class.
        var totalSeats = columns * rows

        // Loop used to generate the seats in the layout.

        // Loop used to generate the seats in the layout.
        for(i in 1..totalSeats) {
            val seat = Button(this)
            var factor = this.resources.displayMetrics.density
            var params = ConstraintLayout.LayoutParams(((400 / studentChairViewGridLayout.columnCount) * factor).toInt(), ((450 / studentChairViewGridLayout.rowCount) * factor).toInt())
            seat.layoutParams = (params)
            seat.text = "Seat #: " + i.toString()
            db.collection("seats")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {

                            if (document["seat"] == i.toString()) {
                                // On this case, then we know the seat is taken.
                                if (document["student"].toString() != "null") {

                                    if (document["student"].toString() == uid) {
                                        studentHasPickedSeat = true
                                    }
                                    // change color to red for button
                                    seatTaken = true
                                    var red = Color.parseColor("#FF0000")
                                    seat.setBackgroundColor(red)
                                    seat.setOnClickListener {
                                        // Nothing, seat taken by another student.
                                    }
                                } else {
                                    // let it be green by default
                                    seatTaken = false
                                    var green = Color.parseColor("#008000")
                                    seat.setBackgroundColor(green)
                                    seat.setOnClickListener {
                                        // Only sets onClickListener for buttons, if the student hasn't already picked a chair.
                                        if (studentHasPickedSeat) {
                                            // Do nothing, student has already chosen a seat.
                                        } else {
                                            takeSeat(i)
                                            var red = Color.parseColor("#FF0000")
                                            seat.setBackgroundColor(red)
                                        }
                                    }
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
            studentChairViewGridLayout.addView(seat)

        }
    }

    /**
     * Function that is used when a student clicks on a chair, it pushes their studentID to the
     * Firestore database for the seats.
     */

    private fun takeSeat(seatNumber: Int) {
        val uid = user!!.uid

        db.collection("seats")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["seat"] == seatNumber.toString()) {
                            // On this case, then we are on the class for the teacher
                            var seatsRef = db.collection("seats").document(document.id.toString())
                            seatsRef.update("student", uid)
                            studentHasPickedSeat = true
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

}
