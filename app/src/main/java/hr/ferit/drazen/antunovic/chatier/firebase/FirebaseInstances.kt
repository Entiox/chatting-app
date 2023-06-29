package hr.ferit.drazen.antunovic.chatier.firebase

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseInstances {
    private val database = Firebase.database("https://chatier-fff4e-default-rtdb.europe-west1.firebasedatabase.app/")
    fun getDatabase(): FirebaseDatabase {
        return database
    }
}
