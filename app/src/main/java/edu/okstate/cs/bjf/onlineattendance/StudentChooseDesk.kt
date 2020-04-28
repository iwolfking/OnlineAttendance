package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_student_choose_desk.*
import kotlinx.android.synthetic.main.activity_teacher_create_desks.*

class StudentChooseDesk : AppCompatActivity() {

    /**
     * Variables that are required to use Firebase, perform Firebase calls in this activity.
     */
    private lateinit var auth: FirebaseAuth
    var user = FirebaseAuth.getInstance().currentUser
    var db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = null

    // testing by using the teacher UID
    var teacher = "ZbnbwWBlsrbliKIBdRyJrlVCNtn1"

    // Variables for the columns/rows of seats in the class. Set as string, convert to Int when needed.
    var numColumns = "0"
    var numRows = "0"
    // TODO: Implement a text field, to show the student how many times they have attended this course.
    private var sessionsToDate = "0"
    var seatTaken: Boolean = false

    // Used to determine, if a student has already chosen a seat.
    private var studentHasPickedSeat = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_choose_desk)

        // Initialize Firebase variables, and get the number of seats to know how to generate them.
        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        getSeats()

    }

    /**
     * Function that gets the number of seats that are in this class, that was set by the teacher.
     * It gets the number of columns/rows, and calculates the number of total seats.
     */

    private fun getSeats() {

        // TODO: Update, so it gets the UID of the teacher from the class.
        val uid = teacher

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
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
