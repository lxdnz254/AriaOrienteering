package com.lxdnz.nz.ariaorienteering.tasks

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*
import com.lxdnz.nz.ariaorienteering.model.User

class UserTask {

    companion object: UserTaskFactory{

        // connect to firebase
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val mDatabaseReference: DatabaseReference = db.getReference("users")

        override fun retrieveTask(uid: String): Task<User> {
            val tcs: TaskCompletionSource<User> = TaskCompletionSource()

            mDatabaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    Log.i("onDataChange", user?.uid + ":" + user!!.firstName)
                    tcs.setResult(user)
                }
            })
            return tcs.task
        }

        override fun createTask(user: User) {
            mDatabaseReference.child(user.uid).setValue(user)
        }
    }
}

interface UserTaskFactory {
    fun retrieveTask(uid: String): Task<User>

    fun createTask(user: User)
}