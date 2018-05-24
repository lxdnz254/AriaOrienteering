package com.lxdnz.nz.ariaorienteering.tasks

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object AdminTask {
    // connect to firebase
    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val mDatabaseReference: DatabaseReference = db.getReference("admin")
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun checkAdmin(name: String?): Task<Boolean> {
        val tcs: TaskCompletionSource<Boolean> = TaskCompletionSource()

        mDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val admin = snapshot.getValue(String::class.java)
                if (name.equals(admin)){
                    tcs.setResult(true)
                }else {
                    tcs.setResult(false)
                }
            }

        })
        return tcs.task
    }
}