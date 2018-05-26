package com.lxdnz.nz.ariaorienteering.services

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.Toast

import com.lxdnz.nz.ariaorienteering.services.location.LocationTracker
import com.lxdnz.nz.ariaorienteering.services.location.LocationTrackerFallback
import com.lxdnz.nz.ariaorienteering.services.location.LocationTrackerProvider

/**
 * Service for operating Location tracking
 */

class LocationService(): Service(), LocationTracker {

    constructor(context: Context) : this() {
        this.mContext = context
        tracker = LocationTrackerFallback(context, type)
    }

    // final Vals
    private val TAG: String = "LocationService"
    private val MY_PREFS = "MyPrefs"
    private val ACTIVE = "active"
    private val type  =  LocationTrackerProvider.ProviderType.GPS //only want to use GPS


    // public variables
    var isServiceRunning: Boolean = false
    var latitude: Double = 0.toDouble() // latitude
    var longitude: Double = 0.toDouble() // longitude
    var update: LocationTracker.LocationUpdateListener? = null

    // Late initialisers
    lateinit var mContext: Context
    lateinit var tracker: LocationTrackerFallback
    lateinit var looper: Looper
    lateinit var locationServiceHandler: LocationServiceHandler
    lateinit var sPref: SharedPreferences

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val locationHandlerThread = HandlerThread("Location Thread", Process.THREAD_PRIORITY_BACKGROUND)
        locationHandlerThread.start()
        looper = locationHandlerThread.looper
        locationServiceHandler = LocationServiceHandler(looper)
        sPref = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        isServiceRunning = true
    }

    override fun onDestroy() {
        Log.d(TAG, "service destroyed")
        Toast.makeText(this, "Location Tracker stopped", Toast.LENGTH_SHORT).show()
        stop()
        isServiceRunning = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        mContext = this.baseContext
        val msg:Message = locationServiceHandler.obtainMessage()
        msg.arg1 = startId
        locationServiceHandler.sendMessage(msg)
        tracker = LocationTrackerFallback(mContext,LocationTrackerProvider.ProviderType.GPS)
        start(update)
        return START_REDELIVER_INTENT
    }

    override fun start() {
        tracker.start()
    }

    override fun start(update: LocationTracker.LocationUpdateListener?) {
        tracker.start(update)
    }

    override fun stop() {
        tracker.stop()
    }

    override fun hasLocation(): Boolean {
        return tracker.hasLocation()
    }

    override fun hasPossiblyStaleLocation(): Boolean {
        return tracker.hasPossiblyStaleLocation()
    }

    override fun getLocation(): Location? {
        if(hasLocation()) {
            return tracker.getLocation()
        }
        else return getPossiblyStaleLocation()
    }

    override fun getPossiblyStaleLocation(): Location? {
        if(hasPossiblyStaleLocation()) {
            return tracker.getPossiblyStaleLocation()
        }
        else return null
    }


    /**
     * Function to show settings alert dialog
     * On pressing Settings button will launch Settings Options
     */
    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(mContext)

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings")

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", DialogInterface.OnClickListener { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        })

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        // Showing Alert Message
        alertDialog.show()
    }

    inner class LocationServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message?) {
            synchronized(this) {
                for(i in 0..9) {
                    try{
                        Log.i(TAG, " running...")
                        Thread.sleep(1000)
                    }catch(e:Exception){
                        Log.i(TAG, e.localizedMessage)
                    }
                    if (!isServiceRunning) {
                        Log.i(TAG, "not running")
                        break
                    }
                }
            }
        }
    }
}