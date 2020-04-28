package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_student_profile.*
import kotlinx.android.synthetic.main.activity_student_profile_teacher_view.*
import kotlinx.android.synthetic.main.activity_teacher_create_desks.*
import kotlinx.android.synthetic.main.activity_teacher_profile.*

class StudentProfileTeacherView : AppCompatActivity() {

    // val testStudentJoeExotic = "g8IuOC8EnfhCOFaEQgv9u0ZVeGH2"
    var student = "null"
    private var courseDocumentID = "GjDJjFRg0ojhZ7GtS54m"
    var sessionsToDate = "1"
    private var studnetsAttendanceToDate = "0"
    var seatNumber = 0
    var studentAttendanceRecordFound = false

    private lateinit var auth: FirebaseAuth
    var user = FirebaseAuth.getInstance().currentUser
    var db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile_teacher_view)

        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        getStudent()
        getCourseName()

        seatNumber = intent.getStringExtra("studentSeat").toInt()

        inClassButton.setOnClickListener {
            // Increment student's attendance number by 1 if they are in class.
            updateNumberOfAttendance()
            val teacherCreateDesksIntent = Intent(this, TeacherCreateDesks::class.java)
            startActivity(teacherCreateDesksIntent)
        }

        notInClassButton.setOnClickListener {
            // Do not increment student's attendance number, as they aren't in class.
            // TODO: Create Method To Clear Seat Button from GridView
            clearSeat(seatNumber.toString())
            val teacherCreateDesksIntent = Intent(this, TeacherCreateDesks::class.java)
            startActivity(teacherCreateDesksIntent)
        }
    }

    /**
    // Updates the UI, when onCreate is called on the activity.
    private fun updateUI() {
        // The user's ID, unique to the Firebase project. Do NOT use this value to
        // authenticate with your backend server, if you have one. Use
        // FirebaseUser.getIdToken() instead.
        val uid = user!!.uid

    } */

    // Displays the course name, depending on the teacher signed in.
    private fun getCourseName() {
        val uid = user!!.uid

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            println("Course Name: " + document["course"])
                            courseDocumentID = document.id.toString()
                            getCourseSessions()
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

    private fun getStudent() {
        db.collection("seats")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["seat"] == seatNumber.toString()) {
                            student = document["student"].toString()
                            getStudentName()
                            getStudentEmail()
                            getProfilePicture()
                            getAttendance()
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

    // Displays Student's Name
    private fun getStudentName() {
        val uid = student

        db.collection("students")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["student"] == uid) {
                            println("Student Name: " + document["firstName"] + " " + document["lastName"])
                            studentNameInTeacherView.text = (document["firstName"].toString() + " " + document["lastName"].toString())
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

    // Displays Student's Email Address
    private fun getStudentEmail() {
        val uid = student

        db.collection("students")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["student"] == uid) {
                            studentEmailInTeacherView.text = (document["email"].toString())
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

    // Displays student's profile picture.
    private fun getProfilePicture() {
        val uid = student
        // Create a storage reference from our app


        /*In this case we'll use this kind of reference*/
        //Download file in Memory
        val profilePictureRef = mStorageRef?.child("pics/$uid")

        val ONE_MEGABYTE = 1024 * 1024 * 50.toLong()
        profilePictureRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
            // Data for "images/island.jpg" is returns, use this as needed

            println("Picture exists")
            studentProfilePictureInTeacherView.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
        }?.addOnFailureListener {
            // Handle any errors
            println("ERROR")
            println("Error" + uid + " doesn't exist.")
        }
    }

    // Used to find the times the student has attended the course.
    private fun getAttendance() {

        val uid = student

        db.collection("attendance")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["student"] == uid) {
                            // On this case, then we are on the class for the teacher
                            studnetsAttendanceToDate = document["attendance"].toString()
                            // COMPLETED: It is not updating properly with the database, may be a sync issue.
                            var percentAttended: Double = (studnetsAttendanceToDate.toDouble() / sessionsToDate.toDouble()) * 100
                            studentAttendance.text = percentAttended.toString() + "%"
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
        val uid = user!!.uid

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            println("Updating Sessions To Date...")
                            println(document["sessions"].toString())
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

    // Used if it is the first attendance in the course of the student. Creates a document on database.
    private fun createAttendanceForStudent(student: String, course: String, attendance: String) {
        val attendance = hashMapOf(
            "student" to student,
            "course" to course,
            "attendance" to attendance
        )

        db.collection("attendance")
            .add(attendance)
            .addOnSuccessListener { documentReference ->
                Log.d(CreateTeacherProfileActivity.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                println("Created attendance")
            }
            .addOnFailureListener { e ->
                Log.w(CreateTeacherProfileActivity.TAG, "Error adding document", e)
            }
    }


    // Function used to update the attendance of the student if verified by the teacher.
    private fun updateNumberOfAttendance() {

        val uid = student

        db.collection("attendance")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["student"] == uid) {
                            // On this case, then we are on the class for the teacher

                            studentAttendanceRecordFound = true

                            var attendanceRef = db.collection("attendance").document(document.id.toString())
                            var attendanceToDate = (document["attendance"].toString().toInt() + 1).toString()
                            attendanceRef.update("attendance", attendanceToDate)
                            println("Submitted data!")
                        } else {
                            Log.d(
                                "Document",
                                document.id + " => " + document.data
                            )
                            println("This is awkward...")
                        }

                    }

                    // COMPLETED: Create attendance record, if one wasn't found.
                    if (!studentAttendanceRecordFound) {
                        val attendance = hashMapOf(
                            "attendance" to "1",
                            "course" to courseDocumentID,
                            "student" to student
                        )

                        // Add a new document with a generated ID
                        db.collection("attendance")
                            .add(attendance)
                            .addOnSuccessListener { documentReference ->
                                Log.d(CreateTeacherProfileActivity.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(CreateTeacherProfileActivity.TAG, "Error adding document", e)
                            }
                    }

                } else {
                    Log.w("Empty", "Error getting documents.", task.exception)
                }
            }
    }

    /**
     * Function used when the student isn't in class, to make the seat available again, so
     * another student may choose the seat.
     */

    private fun clearSeat(seatNumberToClear: String) {
        db.collection("seats")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["seat"] == seatNumber.toString()) {
                            var seatsRef = db.collection("seats").document(document.id.toString())
                            seatsRef.update("student", "null")
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
