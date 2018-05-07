package com.lxdnz.nz.ariaorienteering.services.location

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.lxdnz.nz.ariaorienteering.model.User

class LocationTrackerProvider():  LocationListener, LocationTracker {

    constructor(context: Context, type: ProviderType): this() {
        lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        // get provider
        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    enum class ProviderType {
        NETWORK,
        GPS
    }
    // Final attributes
    private val MIN_UPDATE_DISTANCE: Float = 3f // 3 meters
    private val MIN_UPDATE_TIME: Long = 1000 * 5 // 5 seconds

    // Private variables
    private var provider = ""
    private var lastTime: Long = 0
    private var isRunning = false
    private var listener: LocationTracker.LocationUpdateListener? = null
    private var lastLocation: Location? = null
    //protected variables
    protected var lm: LocationManager? = null

    // uninitialised variables
    lateinit var sharedPreferences: SharedPreferences


    /**
     * LocationTracker Methods
     */

    override fun start() {
        if (isRunning) {
            // already running do nothing
            return
        } else {
            try {
                // the provider is on so start getting updates
                isRunning = true
                lm?.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this)
                return
            }
            catch (e: Exception) {
                e.printStackTrace()
            } catch (se: SecurityException) {
                // open a dialog here to say no permissions granted for GPS/Location Services
            }
        }
    }

    override fun start(update: LocationTracker.LocationUpdateListener?) {
        start()
        listener = update
    }

    override fun stop() {
        lm?.removeUpdates(this)
        isRunning = false
        listener = null
    }

    override fun hasLocation(): Boolean {
        if (lastLocation == null){
            return false
        }
        if(System.currentTimeMillis() - lastTime > 60 * MIN_UPDATE_TIME) {
            return false; // stale (5 mins)
        }
        return true
    }

    override fun hasPossiblyStaleLocation(): Boolean {
        if (lastLocation != null) {
            return true
        }
        try {
            return lm?.getLastKnownLocation(provider) != null

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (se: SecurityException) {
            // open a dialog here to say no permissions granted for GPS/Location Services
        }
        return false
    }

    override fun getLocation(): Location? {
        if(lastLocation == null) {
            return null
        }
        if(System.currentTimeMillis() - lastTime > 60 * MIN_UPDATE_TIME) {
            return null //stale location
        }
        return lastLocation
    }

    override fun getPossiblyStaleLocation(): Location? {
        if(lastLocation != null) {
            return lastLocation
        }
        try {
            return lm?.getLastKnownLocation(provider)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (se: SecurityException) {
            // open a dialog here to say no permissions granted for GPS/Location Services
        }
        return null
    }

    /**
     * android.LocationListener methods
     */
    override fun onLocationChanged(newLoc: Location) {
        val now = System.currentTimeMillis()
        if(listener != null) {
            listener?.onUpdate(lastLocation, lastTime, newLoc, now )
            User.move(newLoc)
        }
        lastLocation = newLoc
        lastTime = now
    }


    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(p0: String?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(p0: String?) {
       //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}