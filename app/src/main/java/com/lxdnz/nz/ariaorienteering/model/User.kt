package com.lxdnz.nz.ariaorienteering.model

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*
import java.util.*

class User {
            var uid: String? = null
            var email: String? = null
            var firstName: String = ""
            var lat: Double = 0.0
            var lon: Double = 0.0
            //var course_object : Course,
            //var target : Marker,
            var active: Boolean = true

    constructor() {
        // Default constructor required for calls to DataSnapshot
    }

    constructor(uid: String?, email: String?, firstName: String, lat: Double, lon: Double, active: Boolean) {
        this.uid = uid
        this.email = email
        this.firstName = firstName
        this.lat = lat
        this.lon = lon
        this.active = active
    }
    // Object and Database creation Factory

    companion object: UserFactory {
        // connect to firebase
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val mDatabaseReference: DatabaseReference = db.getReference("users")

        override fun create(uid: String?, email: String?, firstName: String, lat: Double, lon: Double, active: Boolean): User {
            val user = User(uid, email, firstName, lat, lon, active)
            mDatabaseReference.child(uid).setValue(user)
            return user
        }

        override fun retrieve(uid: String?): Task<User> {
            val tcs:TaskCompletionSource<User>  = TaskCompletionSource()

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

        override fun update(user: User) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun delete(uid: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

interface UserFactory {
    fun create(uid:String?, email:String?, firstName: String, lat: Double, lon: Double, active: Boolean): User

    fun retrieve(uid:String?): Task<User>

    fun update(user: User)

    fun delete(uid: String?)
}