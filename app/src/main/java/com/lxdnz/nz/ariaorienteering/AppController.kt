package com.lxdnz.nz.ariaorienteering

import android.app.Application


class AppController : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: AppController? = null

        fun applicationContext(): AppController {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()

    }

}