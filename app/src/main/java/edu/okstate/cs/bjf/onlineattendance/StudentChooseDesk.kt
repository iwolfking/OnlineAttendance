package edu.okstate.cs.bjf.onlineattendance

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

    // testing by using the teacher UID
    var teacher = "ZbnbwWBlsrbliKIBdRyJrlVCNtn1"

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
        setContentView(R.layout.activity_student_choose_desk)

        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        getSeats()

    }

    /** TODO(3): Implement a Kotlin Class/structure to allow a student to choose
     *           from desks that get loaded from the database;
     *           then submit this to the database to reflect on the teacher's side
     *           which desks have a student in them. Create a grid layout
     *           of desks for the student to see.
     */

    /** TODO(4): Create percentile method, to store the number of times the student
     *           has attended the course, for the student & teacher to be able to see.
     */

    private fun getSeats() {
        val uid = teacher

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

    private fun createDesks(columns: Int, rows: Int) {

        /** TODO(2): Need to have a way to update the GridView or any other view in the
         *           TeacherCreateDesks activity. We are able to set the number of columns/rows
         *           and this gets pushed to the database, and their values are used when called
         *           back down.
         */
        // https://stackoverflow.com/questions/35692984/programmatically-adding-textview-to-grid-layout-alignment-not-proper
        // Sets number of columns in the grid view.
        studentChairViewGridLayout.columnCount = columns
        studentChairViewGridLayout.rowCount = rows


        // Number of total seats in the class.
        var totalSeats = columns * rows

        println("FLAG: No Error Here.")

        // Loop used to generate the seats in the layout.
        // TODO: Find way to variably add buttons to th grid view or any other view
        for(i in 1..totalSeats) {
            val seat = Button(this)
            seat.text = i.toString()
            seat.setOnClickListener {
                takeSeat(seat.text.toString().toInt())
                var red = Color.parseColor("#FF0000")
                seat.setBackgroundColor(red)
            }
            checkSeatTaken(i)
            if (seatTaken) {
                var red = Color.parseColor("#FF0000")
                seat.setBackgroundColor(red)
            } else {
                var green = Color.parseColor("#008000")
                seat.setBackgroundColor(green)
            }
            studentChairViewGridLayout.addView(seat)
            println("BREAKPOINT: Here?")

        }
    }


    private fun checkSeatTaken(seat: Int) {
        db.collection("seats")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["seat"] == seat.toString()) {
                            // On this case, then we know the seat is taken.
                            if (document["student"] == true) {
                                seatTaken = true
                            } else {
                                seatTaken = false
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
