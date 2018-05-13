package com.lxdnz.nz.ariaorienteering.model

import android.location.Location
import com.google.android.gms.tasks.Task
import com.lxdnz.nz.ariaorienteering.tasks.UserTask
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

class User {
            var uid: String? = null
            var email: String? = null
            var firstName: String = ""
            var lat: Double = 0.0
            var lon: Double = 0.0
            var course_object: Course? = null
            var markerList: List<Marker>? = null
            var foundMarker: List<Marker>? = null
            var currentTarget: Marker? = null
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
    // Object creation Factory

    companion object: UserFactory {

        override fun create(uid: String?, email: String?, firstName: String, lat: Double, lon: Double, active: Boolean): User {
            val user = User(uid, email, firstName, lat, lon, active)
            // Put User in DB
            UserTask.createTask(user)
            return user
        }

        override fun retrieve(uid: String?): Task<User> {
            // Retrieve user from DB
            return UserTask.retrieveTask(uid!!)
        }

        override fun update(user: User) {
            UserTask.updateTask(user)
        }

        override fun delete(uid: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun move(location: Location?) {
            UserTask.moveTask(location)
        }

        override fun deactivate(uid: String?): Task<User> {
            return UserTask.deactivateUserTask(uid!!)
        }

        override fun activate(uid: String?): Task<User> {
            return UserTask.activateUserTask(uid!!)
        }

        override fun addCourse(course: Course?) {
            UserTask.addCourseTask(course!!)
        }
    }
}

interface UserFactory {
    fun create(uid:String?, email:String?, firstName: String, lat: Double, lon: Double, active: Boolean): User

    fun retrieve(uid:String?): Task<User>

    fun update(user: User)

    fun delete(uid: String?)

    fun move(location: Location?)

    fun deactivate(uid: String?): Task<User>

    fun activate(uid: String?): Task<User>

    fun addCourse(course: Course?)
}