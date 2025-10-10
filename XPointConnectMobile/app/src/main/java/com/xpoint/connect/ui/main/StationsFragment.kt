package com.xpoint.connect.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xpoint.connect.R
import com.xpoint.connect.utils.showToast

class StationsFragment : Fragment() {

    private val viewModel: StationsViewModel by viewModels()
    private lateinit var stationsAdapter: StationsAdapter
    private lateinit var searchEditText: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerViewStations: RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var emptyStateCard: View
    private lateinit var errorStateCard: View

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupSearchFunctionality()
        setupSwipeRefresh()
        setupMapButton()
        observeViewModel()
        loadStations()
    }

    private fun initializeViews(view: View) {
        searchEditText = view.findViewById(R.id.etSearchStations)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerViewStations = view.findViewById(R.id.recyclerViewStations)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyStateCard = view.findViewById(R.id.emptyStateCard)
        errorStateCard = view.findViewById(R.id.errorStateCard)
    }

    private fun setupRecyclerView() {
        stationsAdapter = StationsAdapter { station ->
            // Handle station click - navigate to station details
            showToast("Station selected: ${station.name}")
        }

        recyclerViewStations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stationsAdapter
        }
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}

                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {
                        val query = s?.toString()?.trim() ?: ""
                        viewModel.searchStations(query)
                    }

                    override fun afterTextChanged(s: Editable?) {}
                }
        )
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            searchEditText.setText("")
            loadStations()
        }

        // Set refresh colors to match app theme
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_light,
                R.color.success
        )
    }

    private fun setupMapButton() {
        view?.findViewById<ImageView>(R.id.ivMapView)?.setOnClickListener {
            // TODO: Navigate to map view
            showToast("Map view coming soon!")
        }

        // Setup refresh button in empty state
        view?.findViewById<View>(R.id.btnRefreshStations)?.setOnClickListener {
            searchEditText.setText("")
            loadStations()
        }

        // Setup retry button in error state
        view?.findViewById<View>(R.id.btnRetry)?.setOnClickListener { loadStations() }
    }

    private fun observeViewModel() {
        // Observe filtered stations for search results
        viewModel.filteredStations.observe(viewLifecycleOwner) { resource ->
            swipeRefreshLayout.isRefreshing = false

            when (resource) {
                is com.xpoint.connect.utils.Resource.Success -> {
                    hideAllStates()
                    resource.data?.let { stations ->
                        if (stations.isEmpty()) {
                            showEmptyState()
                        } else {
                            stationsAdapter.submitList(stations)
                            recyclerViewStations.visibility = View.VISIBLE
                        }
                    }
                }
                is com.xpoint.connect.utils.Resource.Error -> {
                    hideAllStates()
                    showErrorState(resource.message ?: "Failed to load stations")
                }
                is com.xpoint.connect.utils.Resource.Loading -> {
                    hideAllStates()
                    showLoadingState()
                }
            }
        }
    }

    private fun hideAllStates() {
        loadingLayout.visibility = View.GONE
        emptyStateCard.visibility = View.GONE
        errorStateCard.visibility = View.GONE
        recyclerViewStations.visibility = View.GONE
    }

    private fun showLoadingState() {
        loadingLayout.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        emptyStateCard.visibility = View.VISIBLE
    }

    private fun showErrorState(message: String) {
        errorStateCard.visibility = View.VISIBLE
        view?.findViewById<android.widget.TextView>(R.id.tvError)?.text = message
    }

    private fun loadStations() {
        viewModel.loadStations()
    }
}
