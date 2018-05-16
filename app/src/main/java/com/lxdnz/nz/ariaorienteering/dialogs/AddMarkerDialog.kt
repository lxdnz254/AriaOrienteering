package com.lxdnz.nz.ariaorienteering.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.lxdnz.nz.ariaorienteering.R

class AddMarkerDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.add_marker_dialog)
                .setPositiveButton("ADD", DialogInterface.OnClickListener(){
                    dialog: DialogInterface, i: Int ->
                        // ADD the marker

                })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener() {
                    dialog: DialogInterface, id: Int  ->
                        dialog.cancel()
                    })
        return builder.create()
    }
}