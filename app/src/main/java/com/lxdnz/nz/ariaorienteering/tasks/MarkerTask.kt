package com.lxdnz.nz.ariaorienteering.tasks

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object MarkerTask {

    // connect to firebase
    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val mDatabaseReference: DatabaseReference = db.getReference("marker")
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun createTask() {

    }

    fun retrieveTask() {

    }

    fun updateTask() {

    }

    fun deleteTask() {

    }
}