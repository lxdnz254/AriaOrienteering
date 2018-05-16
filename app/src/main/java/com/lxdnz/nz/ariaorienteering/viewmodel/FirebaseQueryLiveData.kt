package com.lxdnz.nz.ariaorienteering.viewmodel

import android.arch.lifecycle.LiveData
import android.os.Handler
import android.util.Log
import com.google.firebase.database.*

class FirebaseQueryLiveData: LiveData<DataSnapshot> {
    val TAG = "FirebaseQueryLiveData"
    var query: Query? = null
    var listener = MyValueEventListener()
    var listenerRemovePending = false
    val handler = Handler()
    val removeListener = Runnable {
        query!!.removeEventListener(listener)
        listenerRemovePending = false
    }
    
    constructor(query: Query?) {
        this.query = query
    }

    constructor(ref:DatabaseReference) {
        this.query = ref
    }

    override fun onActive() {
        Log.d(TAG, "onActive")
        if (listenerRemovePending){
            handler.removeCallbacks(removeListener)
        } else {
            query!!.addValueEventListener(listener)
        }
        listenerRemovePending = false
    }

    override fun onInactive() {
        Log.d(TAG, "onInactive")
        // Listener removal is schedule on a two second delay
        handler.postDelayed(removeListener, 2000)
        listenerRemovePending = true
    }

     inner class MyValueEventListener: ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Cant listen to query" + query, p0.toException())
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            setValue(snapshot)
        }

    }

}