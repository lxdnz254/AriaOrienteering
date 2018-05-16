package com.lxdnz.nz.ariaorienteering.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.Transformations
import android.arch.core.util.Function
import android.support.annotation.NonNull
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lxdnz.nz.ariaorienteering.model.User

class UserViewModel: ViewModel() {

    val USER_REF: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    val mAuth = FirebaseAuth.getInstance()
    val liveData = FirebaseQueryLiveData(USER_REF.child(mAuth.currentUser!!.uid))
    val userLiveData: LiveData<User?> = Transformations.map(liveData, Deserializer())

    inner class Deserializer: Function<DataSnapshot, User?> {

        override fun apply(dataSnapshot: DataSnapshot): User? {
            return dataSnapshot.getValue(User::class.java)
        }
    }

    @NonNull
    fun getDataSnapShotLiveData(): LiveData<DataSnapshot> {
        return liveData
    }

    @NonNull
    fun getLiveUserData(): LiveData<User?> {
        return userLiveData
    }
}