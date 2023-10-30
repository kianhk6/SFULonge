package com.example.sfulounge.ui.components

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

private const val ARG_TITLE = "param21"
private const val ARG_ITEMS = "param23"

class SingleChoiceDialog : DialogFragment() {
    private lateinit var listener: SingleChoiceDialogListener
    private var title: String? = null
    private var items: Array<String>? = null

    interface SingleChoiceDialogListener {
        fun onDialogItemIsSelected(dialog: DialogInterface, selectedItemIdx: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            items = it.getStringArray(ARG_ITEMS)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as SingleChoiceDialogListener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(title)
                .setItems(items ?: throw IllegalStateException("Items cannot be null")) {
                    dialog, which ->
                    listener.onDialogItemIsSelected(dialog, which)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        @JvmStatic
        fun newInstance(
            title: String,
            items: Array<String>
        ) = SingleChoiceDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putStringArray(ARG_ITEMS, items)
            }
        }
    }
}