package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_teacher_profile.*


class TeacherProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    var user = FirebaseAuth.getInstance().currentUser
    var db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_profile)

        auth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference;
        updateUI()

        // Submit Button goes to TeacherCreateDesks
        submitTimes.setOnClickListener {
            val teacherCreateDesksIntent = Intent(this, TeacherCreateDesks::class.java)
            startActivity(teacherCreateDesksIntent)
        }
    }



    private fun updateUI() {
        if (user != null) {

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            val uid = user!!.uid
            getTeacherName()
            getEmailAddress()
            getCourseName()
            getProfilePicture()
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
                            teacherName.text = (document["firstName"].toString() + " " + document["lastName"].toString())
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

    private fun getCourseName() {
        val uid = user!!.uid

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            println("Course Name: " + document["course"])
                            courseName.text = (document["course"].toString())
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

    private fun getEmailAddress() {
        val uid = user!!.uid

        db.collection("teachers")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            println("Email Address: " + document["email"])
                            emailAddress.text = (document["email"].toString())
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
            teacherProfilePicture.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
        }?.addOnFailureListener {
            // Handle any errors
            println("ERROR")
            println("Error" + uid + " doesn't exist.")
        }
    }

}
