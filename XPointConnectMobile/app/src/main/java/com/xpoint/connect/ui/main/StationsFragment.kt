package com.xpoint.connect.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xpoint.connect.R
import com.xpoint.connect.utils.showToast

class StationsFragment : Fragment() {

    private val viewModel: StationsViewModel by viewModels()
    private lateinit var stationsAdapter: StationsAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        observeViewModel()
        loadStations()
    }

    private fun setupRecyclerView(view: View) {
        stationsAdapter = StationsAdapter { station ->
            // Handle station click - navigate to station details
            showToast("Station selected: ${station.name}")
        }

        view.findViewById<RecyclerView>(R.id.recyclerViewStations).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stationsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.stations.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is com.xpoint.connect.utils.Resource.Success -> {
                    resource.data?.let { stations ->
                        stationsAdapter.submitList(stations)
                        view?.findViewById<View>(R.id.progressBar)?.visibility = View.GONE
                        view?.findViewById<RecyclerView>(R.id.recyclerViewStations)?.visibility =
                                View.VISIBLE
                    }
                }
                is com.xpoint.connect.utils.Resource.Error -> {
                    view?.findViewById<View>(R.id.progressBar)?.visibility = View.GONE
                    showToast(resource.message ?: "Failed to load stations")
                }
                is com.xpoint.connect.utils.Resource.Loading -> {
                    view?.findViewById<View>(R.id.progressBar)?.visibility = View.VISIBLE
                    view?.findViewById<RecyclerView>(R.id.recyclerViewStations)?.visibility =
                            View.GONE
                }
            }
        }
    }

    private fun loadStations() {
        viewModel.loadStations()
    }
}
