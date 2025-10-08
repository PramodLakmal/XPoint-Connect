package com.xpoint.connect.ui.operator

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.data.api.ApiClient
import com.xpoint.connect.data.model.Booking
import com.xpoint.connect.data.repository.OperatorRepository
import com.xpoint.connect.utils.showToast
import kotlinx.coroutines.launch

class OperatorDashboardActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPERATOR_ID = "extra_operator_id"
        const val EXTRA_OPERATOR_USERNAME = "extra_operator_username"
    }

    private val operatorRepository = OperatorRepository()
    private val apiService = ApiClient.apiService

    private lateinit var bookingsRecycler: RecyclerView
    private lateinit var progress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_dashboard)

        bookingsRecycler = findViewById(R.id.recyclerBookings)
        progress = findViewById(R.id.progressBar)

        bookingsRecycler.layoutManager = LinearLayoutManager(this)
        bookingsRecycler.adapter = OperatorBookingsAdapter(emptyList())

        val operatorId = intent.getStringExtra(EXTRA_OPERATOR_ID).orEmpty()
        if (operatorId.isBlank()) {
            showToast("Missing operator id")
            finish()
            return
        }

        loadBookingsForOperator(operatorId)
    }

    private fun loadBookingsForOperator(operatorId: String) {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val stationsRes = operatorRepository.getOperatorStations(operatorId)
            if (stationsRes is com.xpoint.connect.utils.Resource.Success && (stationsRes.data?.isNotEmpty() == true)) {
                val firstStationId = stationsRes.data!!.first().id
                val bookingsRes = try { apiService.getBookingsByStation(firstStationId) } catch (e: Exception) { null }
                if (bookingsRes != null && bookingsRes.isSuccessful) {
                    val bookings = bookingsRes.body() ?: emptyList()
                    (bookingsRecycler.adapter as OperatorBookingsAdapter).submit(bookings)
                } else {
                    showToast("Failed to load bookings")
                }
            } else {
                showToast("No stations assigned to this operator")
            }
            progress.visibility = View.GONE
        }
    }
}


