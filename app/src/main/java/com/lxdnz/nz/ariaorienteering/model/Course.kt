package com.lxdnz.nz.ariaorienteering.model

import com.google.android.gms.tasks.Task
import com.lxdnz.nz.ariaorienteering.tasks.CourseTask
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

class Course {
    var id = "Z"
    var year = 5
    var markers: MutableList<Marker> = mutableListOf()

    constructor()

    constructor(id: String, year: Int, markers: MutableList<Marker>) {
        this.id = id
        this.year = year
        this.markers = markers
    }

    companion object: CourseFactory {

        override fun create(id: String, year: Int, markers: MutableList<Marker>): Course {
            return CourseTask.createTask(Course(id, year, markers)).result
        }

        override fun retrieve(id: String): Task<Course> {
            return CourseTask.retrieveTask(id)
        }

        override fun retrieveAll(): List<Course?> {
            return CourseTask.retrieveAllTask().result
        }

        override fun selectRandomCourse(): Task<Course> {
            return CourseTask.selectRandomCourseTask()
        }

        override fun addMarker(courseId: String, marker: Marker) {
            CourseTask.addMarker(courseId, marker)

        }
    }
}

interface CourseFactory {
    fun create(id: String, year: Int, markers: MutableList<Marker>): Course
    fun retrieve(id: String): Task<Course>
    fun retrieveAll(): List<Course?>
    fun selectRandomCourse(): Task<Course>
    fun addMarker(courseId: String, marker: Marker)
}