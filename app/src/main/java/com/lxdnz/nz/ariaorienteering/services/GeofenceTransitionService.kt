package com.lxdnz.nz.ariaorienteering.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import android.app.NotificationManager
import android.media.RingtoneManager
import android.graphics.BitmapFactory
import android.app.PendingIntent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder

import com.lxdnz.nz.ariaorienteering.MainActivity
import com.lxdnz.nz.ariaorienteering.R


@Suppress("DEPRECATION")
class GeofenceTransitionService : IntentService("GeofenceTransitionsIntentService") {

    private val TAG = "GTIntentService"

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            (Log.e(TAG, getErrorString(this, geofencingEvent.errorCode)))
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val geofenceTransitionDetails: String = getGeofenceTransitionDetails(this, geofenceTransition ,triggeringGeofences)
            sendNotification(geofenceTransitionDetails)
            Log.i(TAG, geofenceTransitionDetails)
        } else {

            Log.e(TAG, getString(R.string.geofence_transition_invalid_type + geofenceTransition))
        }
    }

    private fun getGeofenceTransitionDetails(context: Context,
                                             geofenceTransition: Int,
                                             triggeringGeofences: List<Geofence>): String {
        val geofenceTransitionString = getTransitionString(geofenceTransition)
        val triggeringGeofencesIdsList = mutableListOf<Geofence>()
        for (geofence in  triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence)
        }
        val triggeringGeofencesIdsListString = TextUtils.join(", ", triggeringGeofencesIdsList )
        return geofenceTransitionString + ": " + triggeringGeofencesIdsListString
    }

    private fun sendNotification(notificationDetails: String) {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this)
        builder.setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(defaultSoundUri)

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Get an instance of the Notification manager
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }

    private fun getTransitionString(transitionType: Int): String {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> return getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT -> return getString(R.string.geofence_transition_exited)
            else -> return getString(R.string.unknown_geofence_transition)
        }
    }

    fun getErrorString(context: Context, errorCode: Int): String {
        val mResources = context.resources
        when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return mResources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return mResources.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return mResources.getString(R.string.geofence_too_many_pending_intents)
            else -> return mResources.getString(R.string.unknown_geofence_error)
        }

    }

}