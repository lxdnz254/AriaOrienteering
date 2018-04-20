package com.lxdnz.nz.ariaorienteering.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.*
import android.util.Log
import android.location.LocationManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.provider.Settings
import android.support.v4.content.ContextCompat.startActivity
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.widget.Toast


class GPSTracker(): Service(), LocationListener {

    val TAG: String = "GPSTracker"
    lateinit var mContext: Context
    var isServiceRunning: Boolean = false
    lateinit var looper: Looper
    lateinit var myServiceHandler: MyServiceHandler

    // flag for GPS status
    var isGPSEnabled = false

    // flag for network status
    var isNetworkEnabled = false

    // flag for GPS status
    var canGetLocation = false

    var location: Location? = null // location
    var latitude: Double = 0.toDouble() // latitude
    var longitude: Double = 0.toDouble() // longitude

    // The minimum distance to change Updates in meters
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 3f // 3 meters

    // The minimum time between updates in milliseconds
    private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute

    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null


    // used if app is off, but course is still running
    constructor(context: Context) : this() {
        this.mContext = context
        getUserLocation()
    }


    fun getUserLocation(): Location? {
        try {
            locationManager = mContext
                    .getSystemService(LOCATION_SERVICE) as LocationManager?

            // getting GPS status
            isGPSEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager?.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                    Log.d("Network", "Network")
                    if (locationManager != null) {
                        location = locationManager!!
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location != null) { // needs to be not null for GPS location to be passed to app
                        locationManager?.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                        Log.d("GPS Enabled", "GPS Enabled")
                        if (locationManager != null) {
                            location = locationManager!!
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (se: SecurityException) {
            // open a dialog here to say no permissions granted for GPS/Location Services
        }

        return location
    }

    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager?.removeUpdates(this)
        }
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

    override fun onCreate() {
        super.onCreate()
        val handlerThread: HandlerThread = HandlerThread("MyThread", Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread.start()
        looper = handlerThread.looper
        myServiceHandler = MyServiceHandler(looper)
        isServiceRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "service destroyed")
        Toast.makeText(this, "GPS Tracker stopped", Toast.LENGTH_SHORT).show()
        isServiceRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        mContext = this.baseContext
        val msg:Message = myServiceHandler.obtainMessage()
        msg.arg1 = startId
        myServiceHandler.sendMessage(msg)
        getUserLocation()
        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? {
       return null
    }

    override fun onLocationChanged(p0: Location?) {
        // here check for Geofences and store new location to Firebase
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    inner class MyServiceHandler(looper: Looper) : Handler(looper) {

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