package com.lxdnz.nz.ariaorienteering.tasks

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.lxdnz.nz.ariaorienteering.model.Course
import com.lxdnz.nz.ariaorienteering.model.Marker
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import java.util.*

object CourseTask {

    // connect to firebase
    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val mDatabaseReference: DatabaseReference = db.getReference("course")
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun createTask(course: Course): Task<Course> {
        val tcs: TaskCompletionSource<Course> = TaskCompletionSource()
        mDatabaseReference.child(course.id).setValue(course)
        tcs.setResult(course)
        return tcs.task
    }

    fun retrieveTask(id: String): Task<Course> {
        val tcs: TaskCompletionSource<Course> = TaskCompletionSource()

        mDatabaseReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val course = snapshot.getValue(Course::class.java)
                Log.i("onDataChange", course?.id + ":" + course!!.year)
                tcs.setResult(course)
            }
        })
        return tcs.task
    }


    fun retrieveAllTask(): Task<List<Course?>> {
        val tcs: TaskCompletionSource<List<Course?>> = TaskCompletionSource()

        mDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val courses: MutableList<Course?> = mutableListOf()
                for(courseSnapshot in snapshot.children) {
                    val course = courseSnapshot.getValue(Course::class.java)
                    courses.add(course)
                }

                tcs.setResult(courses)
            }
        })
        return tcs.task
    }


    fun selectRandomCourseTask(): Task<Course> {
        val tcs: TaskCompletionSource<Course> = TaskCompletionSource()

        /**
         * Returns a random element.
         */
        fun <E> List<E>.random(): E? = if (size > 0) get(Random().nextInt(size)) else null

        task { retrieveAllTask()} then {
            it.addOnCompleteListener {
                courses -> val course = courses.result.random()
                tcs.setResult(course)}
        }

        return tcs.task
    }

    fun addMarker(courseId: String, marker: Marker) {
        task {
            retrieveTask(courseId)
        } then {
            task -> task.addOnCompleteListener {
                res -> val updateCourse = res.result
                updateCourse.markers.add(marker)
                createTask(updateCourse)
            }
        }
    }
}