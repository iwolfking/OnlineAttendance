package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_create_teacher_profile.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.loginButton

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            loginAccount(emailFieldEditText.text.toString(), passwordFieldEditText.text.toString());

        }
    }

    // Create a new user with a first and last name
    private fun loginAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(LoginActivity.TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        // TODO: Check teacher/student & do intent accordingly. Currently goes teacher only.
                        val teacherIntent = Intent(this, TeacherProfile::class.java)
                        startActivity(teacherIntent)
                    }
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(LoginActivity.TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                    // updateUI(null)
                }
            }
        // [END create_user_with_email]
    }

    companion object {
        const val TAG = "EmailPassword"
    }
}
