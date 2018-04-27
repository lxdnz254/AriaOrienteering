package com.lxdnz.nz.ariaorienteering.tasks

import android.location.Location
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.lxdnz.nz.ariaorienteering.model.User
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

class UserTask {

    companion object: UserTaskFactory{

        // connect to firebase
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val mDatabaseReference: DatabaseReference = db.getReference("users")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

        override fun updateTask(user: User) {
            createTask(user)
        }

        override fun moveTask(location: Location?) {
            task {
                retrieveTask(auth.currentUser!!.uid)
            } then {
                task -> task.addOnCompleteListener {
                    user -> val moveUser = user.result
                    moveUser.lon = location!!.longitude
                    moveUser.lat = location.latitude
                    updateTask(moveUser)
                }
            }
        }

        override fun deactivateUserTask(uid: String): Task<User> {
            val tcs: TaskCompletionSource<User> = TaskCompletionSource()
            task { User.retrieve(uid) } then { task ->
                task.addOnCompleteListener { user ->
                    val deactivateUser = user.result
                    deactivateUser.active = false
                    User.update(deactivateUser)
                    task { User.retrieve(uid) } then {
                        res -> res.addOnCompleteListener { it ->
                        if (it.result.active) {
                            deactivateUserTask(uid)
                        } else {
                            tcs.setResult(it.result)
                        }
                    }
                    }
                }
            }
            return tcs.task
        }

        override fun activateUserTask(uid: String): Task<User> {
            val tcs: TaskCompletionSource<User> = TaskCompletionSource()
            task { User.retrieve(uid) } then { task ->
                task.addOnCompleteListener { user ->
                    val activateUser = user.result
                    activateUser.active = true
                    User.update(activateUser)
                    task {
                        User.retrieve(uid)
                    } then {
                        res -> res.addOnCompleteListener { it ->
                        if (!it.result.active) {
                            activateUserTask(uid)}
                        else {
                            tcs.setResult(it.result)
                            }
                        }
                    }
                }
            }
            return tcs.task
        }
    }
}

interface UserTaskFactory {
    fun retrieveTask(uid: String): Task<User>

    fun createTask(user: User)

    fun updateTask(user: User)

    fun moveTask(location: Location?)

    fun deactivateUserTask(uid: String): Task<User>

    fun activateUserTask(uid: String): Task<User>
}