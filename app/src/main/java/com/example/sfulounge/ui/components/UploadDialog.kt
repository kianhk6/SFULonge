package com.example.sfulounge.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.sfulounge.R

class UploadDialog : DialogFragment() {
    private lateinit var listener: UploadDialogListener

    interface UploadDialogListener {
        fun onGalleryClick()
        fun onCameraClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as UploadDialogListener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = initCustomView(it)

            val builder = AlertDialog.Builder(it)
            builder
                // Inflate and set the layout for the dialog.
                // Pass null as the parent view because it's going in the dialog
                // layout.
                .setView(view)
                .setTitle(it.getString(R.string.messages_upload_dialog_title))

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    @SuppressLint("InflateParams")
    private fun initCustomView(activity: Activity): View {
        val view = activity.layoutInflater.inflate(R.layout.messages_upload_dialog, null)

        val galleryButton: Button = view.findViewById(R.id.gallery)
        val cameraButton: Button = view.findViewById(R.id.camera)

        galleryButton.setOnClickListener {
            dialog?.cancel()
            listener.onGalleryClick()
        }
        cameraButton.setOnClickListener {
            dialog?.cancel()
            listener.onCameraClick()
        }

        return view
    }
}