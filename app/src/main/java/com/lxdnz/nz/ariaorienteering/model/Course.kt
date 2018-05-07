package com.lxdnz.nz.ariaorienteering.model

import com.lxdnz.nz.ariaorienteering.tasks.CourseTask
import nl.komponents.kovenant.task

class Course {
    var id = "Z"
    var year = 5
    var markers: MutableList<Int> = mutableListOf(10,11,12)

    constructor()

    constructor(id: String, year: Int, markers: MutableList<Int>) {
        this.id = id
        this.year = year
        this.markers = markers
    }

    companion object: CourseFactory {

        override fun create(id: String, year: Int, markers: MutableList<Int>): Course {
            return CourseTask.createTask(Course(id, year, markers)).result
        }

        override fun retrieve(id: String): Course {
            return CourseTask.retrieveTask(id).result
        }

        override fun retrieveAll(): List<Course?> {
            return CourseTask.retrieveAllTask().result
        }

        override fun selectRandomCourse(): Course {
            return CourseTask.selectRandomCourseTask().result
        }
    }
}

interface CourseFactory {
    fun create(id: String, year: Int, markers: MutableList<Int>): Course
    fun retrieve(id: String): Course
    fun retrieveAll(): List<Course?>
    fun selectRandomCourse(): Course
}