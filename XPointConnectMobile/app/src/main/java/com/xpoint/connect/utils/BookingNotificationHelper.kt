/**
 * BookingNotificationHelper.kt
 *
 * Purpose: Handles booking-related notifications for XPoint Connect mobile app Author: XPoint
 * Connect Development Team Date: October 7, 2025
 *
 * Description: This class manages notifications for booking status changes, particularly when
 * bookings are approved and QR codes become available. It provides user-friendly notifications to
 * keep users informed of their booking status.
 */
package com.xpoint.connect.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xpoint.connect.R
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.model.BookingStatus
import com.xpoint.connect.ui.booking.BookingDetailsActivity

class BookingNotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "booking_notifications"
        private const val CHANNEL_NAME = "Booking Updates"
        private const val CHANNEL_DESCRIPTION = "Notifications for booking status updates"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    init {
        createNotificationChannel()
    }

    /** Creates notification channel for booking notifications (Android 8.0+) */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                        description = CHANNEL_DESCRIPTION
                        enableVibration(true)
                        setShowBadge(true)
                    }

            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Shows notification when booking is approved and QR code is available */
    fun showBookingApprovedNotification(booking: Booking) {
        if (booking.bookingStatus != BookingStatus.Approved) return

        val intent =
                Intent(context, BookingDetailsActivity::class.java).apply {
                    putExtra("booking_id", booking.id)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        booking.id.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notification =
                NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_qr_code)
                        .setContentTitle("Booking Approved! 🎉")
                        .setContentText(
                                "Your booking at ${booking.chargingStationName} is approved. QR code ready!"
                        )
                        .setStyle(
                                NotificationCompat.BigTextStyle()
                                        .bigText(
                                                "Your booking at ${booking.chargingStationName} has been approved! " +
                                                        "QR code is now available for easy check-in. Tap to view booking details."
                                        )
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setVibrate(longArrayOf(0, 250, 250, 250))
                        .build()

        try {
            NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_BASE + booking.id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            e.printStackTrace()
        }
    }

    /** Shows notification for booking status changes */
    fun showBookingStatusChangeNotification(booking: Booking, previousStatus: BookingStatus) {
        val (title, message) =
                when (booking.bookingStatus) {
                    BookingStatus.Approved -> {
                        "Booking Approved! 🎉" to
                                "Your booking at ${booking.chargingStationName} is approved. QR code ready!"
                    }
                    BookingStatus.CheckedIn -> {
                        "Checked In Successfully ⚡" to
                                "You're checked in at ${booking.chargingStationName}. Charging can begin!"
                    }
                    BookingStatus.Completed -> {
                        "Charging Session Complete 🔋" to
                                "Your charging session at ${booking.chargingStationName} has completed."
                    }
                    BookingStatus.Cancelled -> {
                        "Booking Cancelled" to
                                "Your booking at ${booking.chargingStationName} has been cancelled."
                    }
                    BookingStatus.NoShow -> {
                        "Booking Marked as No Show" to
                                "Your booking at ${booking.chargingStationName} was marked as no show."
                    }
                    else -> return // Don't notify for pending status
                }

        val intent =
                Intent(context, BookingDetailsActivity::class.java).apply {
                    putExtra("booking_id", booking.id)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        booking.id.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val iconRes =
                when (booking.bookingStatus) {
                    BookingStatus.Approved -> R.drawable.ic_qr_code
                    BookingStatus.CheckedIn -> R.drawable.ic_charging
                    BookingStatus.Completed -> R.drawable.ic_check_circle
                    BookingStatus.Cancelled, BookingStatus.NoShow -> R.drawable.ic_cancel
                    else -> R.drawable.ic_qr_code
                }

        val notification =
                NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(iconRes)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

        try {
            NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_BASE + booking.id.hashCode(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /** Cancels notification for a specific booking */
    fun cancelBookingNotification(bookingId: String) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BASE + bookingId.hashCode())
    }
}
