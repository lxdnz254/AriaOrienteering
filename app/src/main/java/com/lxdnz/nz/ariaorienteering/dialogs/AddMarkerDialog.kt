package com.lxdnz.nz.ariaorienteering.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.lxdnz.nz.ariaorienteering.R
import com.lxdnz.nz.ariaorienteering.adapters.CourseAdapter
import com.lxdnz.nz.ariaorienteering.model.Course
import com.lxdnz.nz.ariaorienteering.model.Marker
import com.lxdnz.nz.ariaorienteering.model.types.ImageType
import com.lxdnz.nz.ariaorienteering.services.LocationService

class AddMarkerDialog: DialogFragment() {

    private val TAG = "AddMarkerDialog"
    //TODO: Add these arrays to resources files R.arrays.*
    val courseArray = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
    val courseChecked = booleanArrayOf(false,false,false,false,false,false,false,false,false,false,false,false)
    lateinit var gps: LocationService
    var location: Location? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        getLocation()
        val inflater: LayoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.add_marker_dialog, null)

        val gridView: GridView = view.findViewById(R.id.course_grid_view)
        gridView.adapter = CourseAdapter(requireContext(), courseArray, courseChecked)

        // TODO: Iterate ImageType over spinner

        builder.setView(view)

                .setPositiveButton("ADD", DialogInterface.OnClickListener() {
                    dialog: DialogInterface, i: Int ->
                    // ADD the marker
                    Log.i(TAG, "Adding Marker")
                    // TODO: Add validation or Observer for editMarkerNumber
                    val idText: EditText = view.findViewById(R.id.editMarkerNumber)
                    val id = idText.text.toString().toInt()
                    // Add the marker to the system
                    val marker = Marker.create(id, ImageType.DEFAULT,
                            location!!.latitude, location!!.longitude)
                    // iterate over course arrays and add the marker to the course
                    for (j in 0 until courseArray.size) {
                        if (courseChecked[j]) {
                            Log.i(TAG, "Adding to course:" + courseArray[j])
                            Course.addMarker(courseArray[j], marker)
                        }
                    }
                })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener() {
                    dialog: DialogInterface, id: Int ->
                    dialog.cancel()
                })

        return builder.create()
    }

    /**
     * Get the current Location in to the context of the Marker Dialog
     */
    private fun getLocation() {
        gps = LocationService(requireContext())
        location = gps.getLocation()
    }
}