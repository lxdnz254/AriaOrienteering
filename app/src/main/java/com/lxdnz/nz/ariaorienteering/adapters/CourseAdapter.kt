package com.lxdnz.nz.ariaorienteering.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox

import com.lxdnz.nz.ariaorienteering.R

/**
 * Adapter for the displaying a Checkbox in a view
 */
class CourseAdapter(private val mContext: Context, val courseArray: Array<String>, val booleanArray: BooleanArray) : BaseAdapter() {

    val TAG = "Course Adapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val gridView: View
        if (convertView == null){
            gridView = inflater.inflate(R.layout.course_adapter, null)
            val checkBox: CheckBox = gridView.findViewById(R.id.course_checkBox)
            checkBox.text = courseArray[position]
            checkBox.setOnClickListener(View.OnClickListener { v ->
                Log.i(TAG, "Checkbox clicked")
                if (checkBox.isChecked) {
                    Log.i(TAG, "isChecked " + booleanArray[position])
                    booleanArray[position] = true
                } else {
                    booleanArray[position] = false
                }
            })
        } else {
            gridView = convertView
        }
        return gridView
    }

    override fun getItem(p0: Int): Any? = null

    override fun getItemId(p0: Int): Long = 0L

    override fun getCount(): Int = courseArray.size

}
