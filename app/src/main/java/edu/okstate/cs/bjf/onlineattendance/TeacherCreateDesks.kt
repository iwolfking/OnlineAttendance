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

    // Variables for the columns/rows of seats in the class. Set as string, convert to Int when needed.
    var numColumns = "0"
    var numRows = "0"
    private var sessionsToDate = "0"
    var seatTaken: Boolean = false

    private lateinit var auth: FirebaseAuth
    var user = FirebaseAuth.getInstance().currentUser
    var db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_create_desks)

        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        updateUI()

        // Submit Button goes to TeacherCreateDesks
        submitAttendance.setOnClickListener {
            // Do Nothing.
            updateNumberOfSessions()
        }

        editSeatsButton.setOnClickListener {
            val setColumnsAndRowsForSeatsIntent = Intent(this, SetColumnsAndRowsForSeats::class.java)
            startActivity(setColumnsAndRowsForSeatsIntent)
        }

        testStudentButton.setOnClickListener {
            val studentProfileTeacherViewIntent = Intent(this, StudentProfileTeacherView::class.java)
            startActivity(studentProfileTeacherViewIntent)
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

    private fun getProfilePicture() {
        val uid = user!!.uid
        // Create a storage reference from our app


        /*In this case we'll use this kind of reference*/
        //Download file in Memory
        val profilePictureRef = mStorageRef?.child("pics/$uid")

        val ONE_MEGABYTE = 1024 * 1024 * 50.toLong()
        profilePictureRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
            // Data for "images/island.jpg" is returns, use this as needed

            println("Picture exists")
            teacherProfilePictureCreateDesksImageView.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
        }?.addOnFailureListener {
            // Handle any errors
            println("ERROR")
            println("Error" + uid + " doesn't exist.")
        }
    }

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
                            println("Seats Columns: " + numColumns + " Rows: " + numRows)
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
                            println("This is awkward...")
                        }

                    }
                } else {
                    Log.w("Empty", "Error getting documents.", task.exception)
                }
            }
    }

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
                            println("Submitted data!")
                        } else {
                            Log.d(
                                "Document",
                                document.id + " => " + document.data
                            )
                            println("This is awkward...")
                        }

                    }
                } else {
                    Log.w("Empty", "Error getting documents.", task.exception)
                }
            }
    }


    private fun createDesks(columns: Int, rows: Int) {

        /** TODO(2): Need to have a way to update the GridView or any other view in the
         *           TeacherCreateDesks activity. We are able to set the number of columns/rows
         *           and this gets pushed to the database, and their values are used when called
         *           back down.
         */
        // https://stackoverflow.com/questions/35692984/programmatically-adding-textview-to-grid-layout-alignment-not-proper
        // Sets number of columns in the grid view.
        seatsGridLayout.columnCount = columns
        seatsGridLayout.rowCount = rows



        // Number of total seats in the class.
        var totalSeats = columns * rows

        println("FLAG: No Error Here.")

        // Loop used to generate the seats in the layout.
        // TODO: Find way to variably add buttons to th grid view or any other view
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

    /**
    private fun checkSeatTaken(seatNum: Int) {
        db.collection("seats")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["seat"] == seatNum.toString()) {
                            // On this case, then we know the seat is taken.
                            if (document["color"].toString() == "red") {
                                // change color to red for button
                                seatTaken = true
                                var red = Color.parseColor("#FF0000")
                                seat.setBackgroundColor(red)
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
    }
    */

}
