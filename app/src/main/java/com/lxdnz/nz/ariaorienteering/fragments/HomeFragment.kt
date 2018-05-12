package com.lxdnz.nz.ariaorienteering.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import com.lxdnz.nz.ariaorienteering.R
import com.lxdnz.nz.ariaorienteering.model.Course
import com.lxdnz.nz.ariaorienteering.model.User
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

        home_text.text = getString(R.string.change_home) + ' ' + param2
        var dX = 0f
        var dY = 0f
        var startX = 0f
        var startY = 0f
        startActionButton.setOnTouchListener(View.OnTouchListener {v: View, event: MotionEvent ->
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
                        Toast.makeText(v.context, "Selecting Random Course", Toast.LENGTH_SHORT).show()
                        selectRandomCourse()
                    }
                   true
                }
                else -> false
            }
        })
    }


    private fun selectRandomCourse() {
        task { Course.selectRandomCourse() } then { task ->
            task.addOnCompleteListener { course ->
                course_selected.text = getString(R.string.select_course) + ' ' + course.result.id
            }
        }
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
        task { User.retrieve(FirebaseAuth.getInstance().currentUser?.uid) } then
                { task ->
                    task.addOnCompleteListener { user ->
                        home_text.text = getString(R.string.change_home) + ' ' + user.result.firstName
                    }
                }
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
