package com.lxdnz.nz.ariaorienteering.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import com.lxdnz.nz.ariaorienteering.R
import com.lxdnz.nz.ariaorienteering.model.Course
import com.lxdnz.nz.ariaorienteering.model.Marker
import com.lxdnz.nz.ariaorienteering.model.User
import com.lxdnz.nz.ariaorienteering.model.types.ImageType
import com.lxdnz.nz.ariaorienteering.model.types.MarkerStatus
import com.lxdnz.nz.ariaorienteering.services.LocationService
import com.lxdnz.nz.ariaorienteering.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var gameTime:Long = 0

    private val TAG = "Home Fragment"
    private var toastCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // make changes here to id'd view items
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userViewModel: UserViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        val userLiveData = userViewModel.getLiveUserData()
        userLiveData.observe(this, Observer { user: User? ->
            if (user != null) {
                updateUI(user)
            }
        })

        activateButton(startActionButton)

    }

    fun updateUI(user: User) {
        home_text.text = getString(R.string.change_home) + ' ' + user.firstName
        if (user.courseObject != null) {
            // check for All course markers complete then stop timer
            if (checkMarkersFound(user.courseObject?.markers)) {

                // activate home marker
                if (user.homeMarker != null) {
                    if (!user.homeActive && toastCount == 0) {
                        Toast.makeText(requireContext(), "All markers found, Head for Home", Toast.LENGTH_SHORT).show()
                        User.addHomeMarker(user.homeMarker, true)
                        toastCount++
                    } else {
                        Log.i(TAG, "found markers, home Active")
                        if (user.homeMarker!!.status.equals(MarkerStatus.FOUND) && toastCount == 1) {
                            // do this bit when home marker is found
                            timerMeter.stop()
                            Toast.makeText(requireContext(), "You have finished", Toast.LENGTH_SHORT).show()
                            toastCount++
                            // make startButton visible
                            startActionButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        when (toastCount)
        {
            0 -> {if (user.courseObject != null){
                course_selected.text = getString(R.string.select_course) + ' ' + user.courseObject!!.id
                } else {
                course_selected.text = getString(R.string.no_course)
            }
            }
            1 -> {course_selected.text = "Congratulations! " + user.firstName + " you found all the markers. Head for Home"}
            else -> {course_selected.text = "Congratulations! " + user.firstName + " you made it Home"
                // update user status
                if (user.homeActive) {
                    User.finishCourse(timerMeter.text.toString())
                }
            }
        }

    }

    private fun checkMarkersFound(markers: MutableList<Marker>?): Boolean {

        return markers!!.all{marker -> marker.status.equals(MarkerStatus.FOUND)  }
    }

    private fun activateButton(button: FloatingActionButton) {
        var dX = 0f
        var dY = 0f
        var startX = 0f
        var startY = 0f
        button.setOnTouchListener( {v: View, event: MotionEvent ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    startX = event.rawX
                    startY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.setY(event.rawY + dY)
                    v.setX(event.rawX + dX)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (Math.abs(startX - event.rawX) < 10 && Math.abs(startY - event.rawY)  < 10) {
                        when(button) {
                            startActionButton -> {
                                Toast.makeText(v.context, "Selecting Random Course", Toast.LENGTH_SHORT).show()
                                selectRandomCourse()
                                startTimer()
                            }
                        }

                    }
                    true
                }
                else -> false
            }

        })

    }


    private fun selectRandomCourse() {
        // requires a home target, sent to Firebase as one update to limit calls to DB
        val currentLocation = LocationService(requireContext()).getLocation()

        val homeMarker = Marker(1000, ImageType.DEFAULT, currentLocation!!.latitude, currentLocation.longitude)

        task { Course.selectRandomCourse() } then { task ->
            task.addOnCompleteListener { course ->

                homeMarker.status = MarkerStatus.NOT_FOUND
                User.addCourse(course.result, homeMarker)
            }
        }
    }

    private fun startTimer() {

        //start timer
        timerMeter.base = SystemClock.elapsedRealtime() + gameTime
        timerMeter.start()
        toastCount == 0
        // make start button inaccessible
        startActionButton.visibility = View.GONE
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onResume() {
        super.onResume()
        getCurrentUser()
    }

    private fun getCurrentUser() {
        User.retrieve(FirebaseAuth.getInstance().currentUser?.uid)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
