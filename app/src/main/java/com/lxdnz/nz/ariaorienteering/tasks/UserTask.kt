package com.lxdnz.nz.ariaorienteering.tasks

import android.location.Location
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.lxdnz.nz.ariaorienteering.model.Course
import com.lxdnz.nz.ariaorienteering.model.Marker
import com.lxdnz.nz.ariaorienteering.model.User
import com.lxdnz.nz.ariaorienteering.model.types.MarkerStatus
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

/**
 * An object class that handles tasks for all User class objects
 */

object UserTask {

        // connect to firebase
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val mDatabaseReference: DatabaseReference = db.getReference("users")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()


        fun retrieveTask(uid: String): Task<User> {
            val tcs: TaskCompletionSource<User> = TaskCompletionSource()

            mDatabaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    Log.i("onDataChange", user?.uid + ":" + user?.firstName)
                    tcs.setResult(user)
                }
            })
            return tcs.task
        }

        fun createTask(user: User) {
            mDatabaseReference.child(user.uid).setValue(user)
        }

        fun updateTask(user: User) {
            createTask(user)
        }

        fun moveTask(location: Location?) {
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

        fun addCourseTask(course: Course?, marker: Marker) {
            task {
                retrieveTask(auth.currentUser!!.uid)
            } then {
                task -> task.addOnCompleteListener {
                    user -> val courseUser = user.result
                    // associate the course to the user
                    courseUser.courseObject = course
                    course?.markers?.forEach({marker ->
                        marker.status = MarkerStatus.NOT_FOUND})
                    // add homeMarker and de-activate to current User
                    courseUser.homeActive = false
                    courseUser.homeMarker = marker
                    // update the user
                    updateTask(courseUser)
                }
            }
        }

        fun deactivateUserTask(uid: String): Task<User> {
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

        fun activateUserTask(uid: String): Task<User> {
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

    fun findMarkerTask(marker: Marker) {
        // write the inner function / implementation code
        fun updateMarker(updateMarker: Marker, course: Course?) {

            val findMarker = course!!.markers.find { it -> it.id == updateMarker.id }
            if(findMarker != null){
                val index = course.markers.indexOf(findMarker)
                course.markers.removeAt(index)
                updateMarker.status = MarkerStatus.FOUND
                course.markers.add(index, updateMarker)
            }
        }

        task { retrieveTask(auth.currentUser!!.uid)} then {
            task -> task.addOnCompleteListener {
                user -> val findUser = user.result
                updateMarker(marker, findUser.courseObject)
                updateTask(findUser)
            }
        }
    }

    fun targetMarker(id : String) {
        // write the inner function / implementation code
        fun updateMarkerToTarget(course: Course?) {

            // first find any existing TARGET markers and set to NOT_FOUND
            val targetMarkers = course!!.markers.filter { it -> it.status.equals(MarkerStatus.TARGET) }
            targetMarkers.forEach({marker ->
                val ind = course.markers.indexOf(marker)
                course.markers.removeAt(ind)
                marker.status = MarkerStatus.NOT_FOUND
                course.markers.add(ind, marker)
            })

            // Then update the selected marker
            val findMarker = course.markers.find { it -> it.id == id.toInt()}
            if(findMarker != null){
                val index = course.markers.indexOf(findMarker)
                course.markers.removeAt(index)
                findMarker.status = MarkerStatus.TARGET
                course.markers.add(index, findMarker)
            }
        }

        task { retrieveTask(auth.currentUser!!.uid)} then {
            task -> task.addOnCompleteListener {
                user -> val findUser = user.result
                updateMarkerToTarget(findUser.courseObject)
                updateTask(findUser)
            }
        }
    }

    fun homeMarkerTask(marker: Marker, active: Boolean) {
        task { retrieveTask(auth.currentUser!!.uid)} then {
            task -> task.addOnCompleteListener {
                user -> val findUser = user.result
                findUser.homeActive = active
                findUser.homeMarker = marker
                updateTask(findUser)
            }
        }
    }

    fun finishCourseTask() {
        task { retrieveTask(auth.currentUser!!.uid)} then {
            task -> task.addOnCompleteListener {
                user -> val finishedUser = user.result
                finishedUser.homeActive = false
                finishedUser.courseObject = null
                finishedUser.homeMarker = null
                updateTask(finishedUser)
            }
        }


    }

}
