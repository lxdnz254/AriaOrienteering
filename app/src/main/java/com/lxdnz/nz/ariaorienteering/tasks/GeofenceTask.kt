package com.lxdnz.nz.ariaorienteering.tasks

import com.lxdnz.nz.ariaorienteering.BuildConfig
import com.lxdnz.nz.ariaorienteering.dialogs.AddMarkerDialog
import com.lxdnz.nz.ariaorienteering.model.Marker

object GeofenceTask {

    val NEW_GEOFENCE_NUMBER = BuildConfig.APPLICATION_ID + ".NEW_GEOFENCE_NUMBER"
    val GEOFENCE_EXPIRATION_IN_HOURS: Long = 6
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 1000
    val GEOFENCE_RADIUS_IN_METERS = 5f // 5m

    /**
     * receive location and create fence around it
     */
    fun addGeofence(marker: Marker) {

    }

    /**
     * remove the Geofence
     */
    fun removeGeofence(marker: Marker) {

    }

    /**
     * Add a Boundary area, not a circle around a marker
     */
    fun addGeoBoundary() {

    }
}