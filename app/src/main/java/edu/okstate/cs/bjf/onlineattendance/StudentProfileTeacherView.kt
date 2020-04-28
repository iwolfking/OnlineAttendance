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


    // Sent through Intent, this is the seat that was selected by the teacher.
    var seatNumber = 0

    /**
     * Variables that are required to use Firebase, perform Firebase calls in this activity.
     */
    private lateinit var auth: FirebaseAuth
    var user = FirebaseAuth.getInstance().currentUser
    var db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = null

    /**
     * Variables used in relation to the student's profile. student is going to be the ID pulled
     * from the Firebase database. sATD is going to be the times this student has attended this
     * particular course. Defaults to 0, assuming it may be their first time attending the course.
     * sARF is going to allow us to know if we need to create an attendance record, if this is their
     * first time attending the course or not.
     */
    private var student = "null"
    private var studentsAttendanceToDate = "0"
    private var studentAttendanceRecordFound = false

    /**
     * Variables that are related to the current course, that is being attended by the student.
     * cDID is pulled from the teacher that is signed in, default currently to our test teacher's
     * course. sTD is pulled from the Firebase database, and is related to how many times the course
     * has occurred. This is generated when the teacher submits attendance at the end of each course
     * it increments the sTD by one value.
     */
    private var courseDocumentID = "GjDJjFRg0ojhZ7GtS54m"
    var sessionsToDate = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile_teacher_view)

        /**
         * First, we get the seat that is passed through the Intent via the seat pressed by the
         * teacher. Then we initialize our values needed for this activity. Namely, the Firebase/
         * Firestore reference. Then we get the student through the seat number, and then the
         * course name is generated from the current teacher signed in.
         */
        seatNumber = intent.getStringExtra("studentSeat").toInt()
        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        getStudent()
        getCourseName()

        // "In Class" button's action, if pressed by the teacher.
        inClassButton.setOnClickListener {
            // Increment student's attendance number by 1 if they are in class.
            updateNumberOfAttendance()
            val teacherCreateDesksIntent = Intent(this, TeacherCreateDesks::class.java)
            startActivity(teacherCreateDesksIntent)
        }

        // "Not In Class" button's action, if pressed by the teacher.
        notInClassButton.setOnClickListener {
            // Does not increment student's attendance number, as they aren't in class.

            // Clearing seat, so that another student may select this seat. Then go back an activity.
            clearSeat(seatNumber.toString())
            val teacherCreateDesksIntent = Intent(this, TeacherCreateDesks::class.java)
            startActivity(teacherCreateDesksIntent)
        }
    }

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

    // Function that gets the student's information, and displays it on the activity.
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

    // Gets the student's name, and is displayed by getStudent()
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

    // Gets the student's email, and is displayed by getStudent()
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

    // Gets the student's profile picture, and is displayed by getStudent()
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
                            studentsAttendanceToDate = document["attendance"].toString()
                            // COMPLETED: It is not updating properly with the database, may be a sync issue.
                            var percentAttended: Double = (studentsAttendanceToDate.toDouble() / sessionsToDate.toDouble()) * 100
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
