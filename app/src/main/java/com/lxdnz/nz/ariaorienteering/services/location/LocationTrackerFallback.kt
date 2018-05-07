package com.lxdnz.nz.ariaorienteering.services.location

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager

class LocationTrackerFallback(): LocationTracker,LocationTracker.LocationUpdateListener {

    constructor(context: Context, type: LocationTrackerProvider.ProviderType): this() {
        gps = LocationTrackerProvider(context, LocationTrackerProvider.ProviderType.GPS)
        net = LocationTrackerProvider(context, LocationTrackerProvider.ProviderType.NETWORK)
    }

    private var isRunning = false
    private var listener: LocationTracker.LocationUpdateListener? = null

    var lastTime: Long = 0
    var lastLocation: Location? = null


    lateinit var gps: LocationTrackerProvider
    lateinit var net: LocationTrackerProvider

    override fun start() {
        if(isRunning) {
            // already running do nothing
            return
        }
        // Start both
        gps.start(this)
        net.start(this)
        isRunning = true
    }

    override fun start(update: LocationTracker.LocationUpdateListener?) {
        start()
        listener = update
    }

    override fun stop() {
        if (isRunning) {
            gps.stop()
            net.stop()
            isRunning = false
            listener = null
        }
    }

    override fun hasLocation(): Boolean {
        // if either has location use it
        return gps.hasLocation() || net.hasLocation()
    }

    override fun hasPossiblyStaleLocation(): Boolean {
        // if either has location use it
        return gps.hasPossiblyStaleLocation() || net.hasPossiblyStaleLocation()
    }

    override fun getLocation(): Location? {
        var ret = gps.getLocation()
        if (ret == null) {
            ret = net.getLocation()
        }
        return ret
    }

    override fun getPossiblyStaleLocation(): Location? {
        var ret = gps.getPossiblyStaleLocation()
        if (ret == null) {
            ret = net.getPossiblyStaleLocation()
        }
        return ret
    }

    override fun onUpdate(oldLoc: Location?, oldTime: Long, newLocation: Location, newTime: Long) {
        var update = false

        // We should only update if there is no last location
        if (lastLocation == null) {
            update = true
        }
        else if(lastLocation?.provider.equals(newLocation.provider)) {
            update = true
        }
        else if(newLocation.provider.equals(LocationManager.GPS_PROVIDER)) {
            update = true
        }
        else if(newTime - lastTime > 5 * 1000) {//5 seconds
            update = true
        }
        if(update) {
            if (listener != null) {
                listener?.onUpdate(lastLocation, lastTime, newLocation, newTime)
            }
            lastLocation = newLocation
            lastTime = newTime
        }
    }
}