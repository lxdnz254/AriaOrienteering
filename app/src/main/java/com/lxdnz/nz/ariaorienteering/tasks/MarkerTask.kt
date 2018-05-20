package com.lxdnz.nz.ariaorienteering.tasks


import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lxdnz.nz.ariaorienteering.model.Marker

object MarkerTask {

    // connect to firebase
    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val mDatabaseReference: DatabaseReference = db.getReference("marker")
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun createTask(marker: Marker): Task<Marker> {
        val tcs: TaskCompletionSource<Marker> = TaskCompletionSource()
        mDatabaseReference.child(marker.id.toString()).setValue(marker)
        tcs.setResult(marker)
        return tcs.task
    }

    fun retrieveTask() {

    }

    fun updateTask() {

    }

    fun deleteTask() {

    }
}