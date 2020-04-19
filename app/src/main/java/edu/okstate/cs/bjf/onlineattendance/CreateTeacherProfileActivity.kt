package edu.okstate.cs.bjf.onlineattendance

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_teacher_profile.*


class CreateTeacherProfileActivity : AppCompatActivity() {

    /* Resource Found On How To Choose Profile Picture: https://devofandroid.blogspot.com/2018/09/pick-image-from-gallery-android-studio_15.html  */

    // Variables Needed via Firebase
    private var mAuth: FirebaseAuth? = null
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_teacher_profile)

        chooseProfilePcitureButton.setOnClickListener {
            //check runtime permission
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE)
            }
            else{
                //permission already granted
                pickImageFromGallery()
            }
        }

        mAuth = FirebaseAuth.getInstance();

        submitNewTeacherProfile.setOnClickListener {
            // createAccount(emailEditText.text.toString(), passwordEditText.text.toString());
            // addCourseToFirestore()

        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private const val IMAGE_PICK_CODE = 1000
        //Permission code
        const val PERMISSION_CODE = 1001
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            chooseProfilePcitureButton.setImageURI(data?.data)
        }
    }

    /**
     * Commenting out the below code, so we may try to re-implement adding a teacher
     * to the Firebase database. This works, but isn't the type of functionality
     * we are looking for.
     *
    private fun createAccount(email: String, password: String) {
        Log.d("Created Account", "createAccount:$email")
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Created User", "createUserWithEmail:success")
                    val user = mAuth!!.currentUser
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        "Failed User Creation",
                        "createUserWithEmail:failure",
                        task.exception
                    )
                }

                // ...
            }
    }

    private fun addUserNameToUser(user: FirebaseUser?) {
        val username: String = firstNameEditText.text.toString() + " " + lastNameEditText.text.toString()
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Username: ", "User profile updated.")
                }
            }
    }

    private fun addCourseToFirestore() {
        // Create a new user with a first and last name

        // Create a new user with a first and last name
        val course: MutableMap<String, Any> = HashMap()
        course["teacher"] = firstNameEditText.text.toString() + " " + lastNameEditText.text.toString()
        course["teacher-email"] = emailEditText.text.toString()
        course["course-name"] = courseNameEditText.text.toString()
        course["course-start"] = "0000"
        course["course-end"] = "0000"
        course["id"] = "0"
        course["building"] = "Bldg 1"
        course["room"] = "Rm 101"



        // Add a new document with a generated ID
        db.collection("Courses")
            .add(course)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "Course Added",
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
                println("Made it!")
                val teacherProfileIntent = Intent(this, TeacherProfile::class.java)
                startActivity(teacherProfileIntent)
            }
            .addOnFailureListener { e -> Log.w("Course Not Added", "Error adding document", e) }
    } */

}
