/**
 * EVOwnerToast.kt
 *
 * Purpose: Custom toast implementation for EV Owner screens with XPoint branding Author: XPoint
 * Connect Development Team Date: October 10, 2025
 *
 * Description: This utility provides custom toast messages specifically for EV Owner screens with
 * XPoint logo integration and consistent green theme styling.
 *
 * Key Features:
 * - XPoint logo integration in toast messages
 * - Custom styling with green theme
 * - Different toast types (success, error, info, warning)
 * - Consistent branding across EV Owner screens
 */
package com.xpoint.connect.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xpoint.connect.R

/** Custom toast utility for EV Owner screens with XPoint branding */
object EVOwnerToast {

    enum class ToastType {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }

    /** Shows a custom toast with XPoint logo for EV Owner screens */
    fun show(
            context: Context,
            message: String,
            type: ToastType = ToastType.INFO,
            duration: Int = Toast.LENGTH_SHORT
    ) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val text = layout.findViewById<TextView>(R.id.toast_text)
        val background = layout.findViewById<View>(R.id.toast_background)

        // Set the XPoint logo
        icon.setImageResource(R.drawable.ic_xpoint_logo)

        // Set the message
        text.text = message

        // Set colors based on toast type
        when (type) {
            ToastType.SUCCESS -> {
                background.background =
                        ContextCompat.getDrawable(context, R.drawable.toast_success_background)
                text.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            ToastType.ERROR -> {
                background.background =
                        ContextCompat.getDrawable(context, R.drawable.toast_error_background)
                text.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            ToastType.WARNING -> {
                background.background =
                        ContextCompat.getDrawable(context, R.drawable.toast_warning_background)
                text.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            ToastType.INFO -> {
                background.background =
                        ContextCompat.getDrawable(context, R.drawable.toast_info_background)
                text.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
        }

        val toast = Toast(context)
        toast.duration = duration
        toast.view = layout
        toast.show()
    }
}

/** Extension function for Context to show custom EV Owner toast */
fun Context.showEVOwnerToast(
        message: String,
        type: EVOwnerToast.ToastType = EVOwnerToast.ToastType.INFO,
        duration: Int = Toast.LENGTH_SHORT
) {
    EVOwnerToast.show(this, message, type, duration)
}

/** Extension function for Fragment to show custom EV Owner toast */
fun Fragment.showEVOwnerToast(
        message: String,
        type: EVOwnerToast.ToastType = EVOwnerToast.ToastType.INFO,
        duration: Int = Toast.LENGTH_SHORT
) {
    requireContext().showEVOwnerToast(message, type, duration)
}
