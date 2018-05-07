package com.lxdnz.nz.ariaorienteering.services.location

import android.location.Location

interface LocationTracker {
    interface LocationUpdateListener {
        fun onUpdate(oldLoc: Location?,oldTime: Long, newLocation: Location, newTime: Long)
    }

    fun start()
    fun start(update: LocationUpdateListener?)
    fun stop()
    fun hasLocation():Boolean
    fun hasPossiblyStaleLocation():Boolean
    fun getLocation():Location?
    fun getPossiblyStaleLocation():Location?
}