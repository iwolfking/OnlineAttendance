package edu.okstate.cs.bjf.onlineattendance

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_teacher_profile.*
import java.io.ByteArrayOutputStream


class CreateTeacherProfileActivity : AppCompatActivity() {

    /* Resource Found On How To Choose Profile Picture: https://devofandroid.blogspot.com/2018/09/pick-image-from-gallery-android-studio_15.html  */

    // Firebase variables
    private lateinit var auth: FirebaseAuth
    var db = FirebaseFirestore.getInstance()
    private var storageRef: StorageReference? = null

    private lateinit var imageUri : Uri

    private lateinit var profileBitmap: Bitmap


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

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // mStorageRef = FirebaseStorage.getInstance().reference;
        // [END initialize_auth]

        submitNewTeacherProfile.setOnClickListener {
            createAccount(emailEditText.text.toString(), passwordEditText.text.toString());

        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
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
            profileBitmap = (chooseProfilePcitureButton.drawable as BitmapDrawable).bitmap
        }
    }

    // Create a new user with a first and last name
    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        createCourse(user.uid.toString(), courseNameEditText.text.toString())
                        uploadImageAndSaveUri(profileBitmap)
                        val loginIntent = Intent(this, LoginActivity::class.java)
                        createTeacher(user.uid.toString(), firstNameEditText.text.toString(), lastNameEditText.text.toString(), emailEditText.text.toString())
                        startActivity(loginIntent)
                    }
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    // updateUI(null)
                }
            }
        // [END create_user_with_email]
    }

    private fun createCourse(teacher: String, courseName: String) {
        val course = hashMapOf(
            "teacher" to teacher,
            "course" to courseName
        )

        // Add a new document with a generated ID
        db.collection("courses")
            .add(course)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun createTeacher(teacher: String, firstName: String, lastName: String, email: String) {
        val course = hashMapOf(
            "teacher" to teacher,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )

        db.collection("teachers")
            .add(course)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun uploadImageAndSaveUri(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val upload = storageRef.putBytes(image)

        upload.addOnCompleteListener { uploadTask ->
            // progressbar_pic.visibility = View.INVISIBLE

            if (uploadTask.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let {
                        imageUri = it
                        // activity?.toast(imageUri.toString())
                        // image_view.setImageBitmap(bitmap)
                    }
                }
            } else {
                uploadTask.exception?.let {
                    // activity?.toast(it.message!!)
                }
            }
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
        // private const val RC_MULTI_FACTOR = 9005
        //image pick code
        private const val IMAGE_PICK_CODE = 1000
        //Permission code
        const val PERMISSION_CODE = 1001
    }

}
