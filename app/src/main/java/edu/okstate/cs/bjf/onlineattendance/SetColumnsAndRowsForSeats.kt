package edu.okstate.cs.bjf.onlineattendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_set_columns_and_rows_for_seats.*
import kotlinx.android.synthetic.main.activity_teacher_profile.*

class SetColumnsAndRowsForSeats : AppCompatActivity() {

    // Firebase variables
    private lateinit var auth: FirebaseAuth
    var user = FirebaseAuth.getInstance().currentUser
    var db = FirebaseFirestore.getInstance()
    private var storageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_columns_and_rows_for_seats)

        auth = FirebaseAuth.getInstance()

        submitColumnsAndRowsButton.setOnClickListener {
            setColumnsAndRowsOnDatabase(numColumnsEditText.text.toString(), numRowsEditText.text.toString())
            val teacherCreateDesksIntent = Intent(this, TeacherCreateDesks::class.java)
            startActivity(teacherCreateDesksIntent)
        }
    }

    private fun setColumnsAndRowsOnDatabase(columns: String, rows: String) {

            val uid = user!!.uid

        db.collection("courses")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        if (document["teacher"] == uid) {
                            // On this case, then we are on the class for the teacher
                            println("Document: " + document.id.toString() + " Columns: " + columns + " Rows: " + rows)
                            var courseRef = db.collection("courses").document(document.id.toString())

                            courseRef.update("columns", columns, "rows", rows)
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
}
