package com.lxdnz.nz.ariaorienteering.tasks

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lxdnz.nz.ariaorienteering.model.Result

object ResultTask {

    // connect to firebase
    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val mDatabaseReference: DatabaseReference = db.getReference("results")

    fun create(result: Result) {
        mDatabaseReference.child(result.uid).setValue(result)
    }
}