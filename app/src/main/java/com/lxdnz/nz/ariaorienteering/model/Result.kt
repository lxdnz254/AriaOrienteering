package com.lxdnz.nz.ariaorienteering.model

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.lxdnz.nz.ariaorienteering.tasks.ResultTask
import java.util.*

class Result {
    var uid: String? = null
    var name: String? = null
    var time: String? = null
    var course: String? = null

    constructor() {

    }

    constructor(uid: String?, name: String?, time: String?, course: String?) {
        this.uid = uid
        this.name = name
        this.time = time
        this.course = course
    }

    companion object: ResultFactory {
        override fun create(uid: String?, name: String?, time: String?, course: String?): Result {
            val result = Result(uid, name, time, course)
            ResultTask.create(result)
            return result
        }

        override fun retrieve(uid: String?): Task<Result> {
            val tcs: TaskCompletionSource<Result> = TaskCompletionSource()
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

            return tcs.task
        }

        override fun update(result: Result) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun delete(uid: String?) {
           //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}

interface ResultFactory {
    fun create (uid: String?, name: String?, time: String?, course: String?): Result
    fun retrieve (uid: String?): Task<Result>
    fun update (result: Result)
    fun delete (uid: String?)
}