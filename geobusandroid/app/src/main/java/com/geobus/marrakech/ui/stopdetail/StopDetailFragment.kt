package com.geobus.marrakech.ui.stopdetail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.geobus.marrakech.R
import com.geobus.marrakech.databinding.FragmentStopDetailBinding
import com.geobus.marrakech.model.BusPosition
import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.model.TimeEstimation
import com.geobus.marrakech.repository.BusPositionRepository
import com.geobus.marrakech.repository.StopRepository
import com.geobus.marrakech.ui.adapter.BusAdapter
import com.geobus.marrakech.util.LocationManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.*

private const val TAG = "StopDetailFragment"

/**
 * Fragment pour afficher les détails d'une station avec les bus
 */
class StopDetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentStopDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StopDetailViewModel
    private lateinit var locationManager: LocationManager
    private lateinit var busAdapter: BusAdapter

    private val args: StopDetailFragmentArgs by navArgs()
    private var map: GoogleMap? = null

    // Repositories
    private val stopRepository = StopRepository()
    private val busRepository = BusPositionRepository()

    // Timer pour rafraîchissement des bus
    private var refreshTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStopDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "StopDetailFragment créé pour station ID: ${args.stopId}")

        // Initialiser le ViewModel
        viewModel = ViewModelProvider(this)[StopDetailViewModel::class.java]

        // Initialiser le gestionnaire de localisation
        locationManager = LocationManager(requireContext())

        // Configurer l'adaptateur pour la liste des bus
        setupBusRecyclerView()

        // Configurer la carte
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapPreview) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurer les boutons
        setupNavigationButton()

        // Observer les données
        observeViewModel()
        observeLocation()

        // Charger les données de la station
        loadStopData(args.stopId)

        // Démarrer le rafraîchissement automatique des bus
        startBusRefresh()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - Redémarrage du rafraîchissement des bus")
        startBusRefresh()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause - Arrêt du rafraîchissement des bus")
        stopBusRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopBusRefresh()
        _binding = null
        locationManager.stopLocationUpdates()
    }

    /**
     * Configuration du bouton de navigation
     */
    private fun setupNavigationButton() {
        binding.btnOpenMaps.setOnClickListener {
            viewModel.selectedStop.value?.let { stop ->
                showNavigationDialog(stop)
            }
        }
    }

    /**
     * Affiche un dialog de choix de navigation
     */
    private fun showNavigationDialog(stop: Stop) {
        val options = arrayOf(
            "🗺️ Voir sur la carte de l'app",
            "📱 Ouvrir Google Maps",
            "🧭 Afficher l'itinéraire à pied"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Navigation vers ${stop.stopName}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToStopOnMap(stop)
                    1 -> openGoogleMapsExternal(stop)
                    2 -> showWalkingDirections(stop)
                }
            }
            .show()
    }

    /**
     * Navigue vers la station sur la carte de l'app
     */
    private fun navigateToStopOnMap(stop: Stop) {
        try {
            // Créer un bundle avec les arguments attendus par MapFragment
            val bundle = Bundle().apply {
                putFloat("targetLat", stop.latitude.toFloat())
                putFloat("targetLon", stop.longitude.toFloat())
                putString("targetName", stop.stopName)
                putLong("stopId", stop.stopId)
            }

            // Utiliser l'ID d'action correct et passer les arguments
            findNavController().navigate(R.id.action_stop_detail_to_map, bundle)
            Log.d(TAG, "Navigation vers carte pour station: ${stop.stopName}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur navigation: ${e.message}")
            Snackbar.make(binding.root, "Erreur de navigation", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Ouvre Google Maps externe
     */
    private fun openGoogleMapsExternal(stop: Stop) {
        val userLocation = locationManager.locationLiveData.value
        val origin = if (userLocation != null) {
            "${userLocation.latitude},${userLocation.longitude}"
        } else {
            "31.6295,-7.9811" // Centre de Marrakech par défaut
        }

        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&origin=$origin" +
                    "&destination=${stop.latitude},${stop.longitude}" +
                    "&travelmode=walking"
        )

        try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Impossible d'ouvrir Google Maps", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Affiche les directions à pied dans l'app
     */
    private fun showWalkingDirections(stop: Stop) {
        val userLocation = locationManager.locationLiveData.value
        if (userLocation != null) {
            val distance = stop.distanceTo(userLocation.latitude, userLocation.longitude)
            val walkingTime = stop.estimateWalkingTime(userLocation.latitude, userLocation.longitude)

            val directions = """
                🚶 Itinéraire à pied vers ${stop.stopName}

                📍 Distance: ${distance.toInt()} m
                ⏱️ Temps estimé: ${walkingTime.toInt()} min

                🧭 Direction: ${getDirection(userLocation.latitude, userLocation.longitude, stop.latitude, stop.longitude)}
            """.trimIndent()

            AlertDialog.Builder(requireContext())
                .setTitle("Itinéraire à pied")
                .setMessage(directions)
                .setPositiveButton("OK", null)
                .setNeutralButton("Voir sur carte") { _, _ ->
                    navigateToStopOnMap(stop)
                }
                .show()
        } else {
            Snackbar.make(binding.root, "Position non disponible", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Calcule la direction approximative
     */
    private fun getDirection(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): String {
        val deltaLat = toLat - fromLat
        val deltaLon = toLon - fromLon

        return when {
            abs(deltaLat) > abs(deltaLon) -> {
                if (deltaLat > 0) "Nord" else "Sud"
            }
            deltaLon > 0 -> "Est"
            else -> "Ouest"
        }
    }

    /**
     * Configuration du RecyclerView pour les bus
     */
    private fun setupBusRecyclerView() {
        busAdapter = BusAdapter { bus ->
            showBusDetails(bus)
        }

        binding.recyclerViewBuses.apply {
            adapter = busAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        Log.d(TAG, "RecyclerView configuré pour les bus")
    }

    /**
     * Démarre le rafraîchissement automatique des bus toutes les 10 secondes
     */
    private fun startBusRefresh() {
        stopBusRefresh()

        refreshTimer = Timer()
        refreshTimer?.schedule(0, 10000) {
            Log.d(TAG, "Rafraîchissement automatique des bus pour station ${args.stopId}")
            loadBusesForStop()
        }
        Log.d(TAG, "Rafraîchissement des bus démarré")
    }

    /**
     * Arrête le rafraîchissement automatique
     */
    private fun stopBusRefresh() {
        refreshTimer?.cancel()
        refreshTimer = null
        Log.d(TAG, "Rafraîchissement des bus arrêté")
    }

    /**
     * Charge les bus pour cette station
     */
    private fun loadBusesForStop() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Chargement des bus pour station ${args.stopId}")

                var buses = busRepository.getBusesGoingToStop(args.stopId)

                if (buses.isNullOrEmpty()) {
                    Log.d(TAG, "Aucun bus spécifique trouvé, récupération de tous les bus")
                    val allBuses = busRepository.getAllLatestPositions()

                    if (!allBuses.isNullOrEmpty()) {
                        val currentStop = viewModel.selectedStop.value
                        if (currentStop != null) {
                            buses = filterBusesNearStop(allBuses, currentStop)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (!buses.isNullOrEmpty()) {
                        val currentStop = viewModel.selectedStop.value
                        if (currentStop != null) {
                            val busesWithTimes = busRepository.calculateArrivalTimes(
                                buses, currentStop.latitude, currentStop.longitude
                            )
                            updateBusList(busesWithTimes)
                        } else {
                            updateBusList(buses)
                        }
                    } else {
                        showNoBusesMessage()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du chargement des bus: ${e.message}")
                withContext(Dispatchers.Main) {
                    showNoBusesMessage()
                }
            }
        }
    }

    /**
     * Filtre les bus proches d'une station
     */
    private fun filterBusesNearStop(allBuses: List<BusPosition>, stop: Stop): List<BusPosition> {
        return allBuses.filter { bus ->
            val distance = bus.distanceTo(stop.latitude, stop.longitude)
            distance <= 2000.0 // 2km de rayon
        }.sortedBy { bus ->
            bus.distanceTo(stop.latitude, stop.longitude)
        }
    }

    /**
     * Met à jour la liste des bus
     */
    private fun updateBusList(buses: List<BusPosition>) {
        Log.d(TAG, "Mise à jour de la liste avec ${buses.size} bus")

        val sortedBuses = buses.sortedBy { it.minutesUntilArrival ?: Int.MAX_VALUE }
        busAdapter.updateBuses(sortedBuses)

        binding.tvBusCount.text = getString(R.string.bus_count, buses.size)

        val nextBus = sortedBuses.firstOrNull()
        nextBus?.let { bus ->
            val message = when {
                bus.minutesUntilArrival != null && bus.minutesUntilArrival!! <= 1 ->
                    "Bus ${bus.ligne} arrive maintenant!"
                bus.minutesUntilArrival != null ->
                    "Prochain bus: ${bus.ligne} dans ${bus.minutesUntilArrival} min"
                else ->
                    "Prochain bus: ${bus.ligne} → ${bus.destination ?: "..."}"
            }

            binding.tvNextBus.text = message
            binding.tvNextBus.visibility = View.VISIBLE
        } ?: run {
            binding.tvNextBus.visibility = View.GONE
        }

        binding.cardBusInfo.visibility = View.VISIBLE
    }

    /**
     * Affiche un message quand aucun bus n'est trouvé
     */
    private fun showNoBusesMessage() {
        Log.d(TAG, "Aucun bus trouvé pour cette station")
        binding.tvBusCount.text = "Aucun bus en approche actuellement"
        binding.tvNextBus.visibility = View.GONE
        busAdapter.updateBuses(emptyList())
        binding.cardBusInfo.visibility = View.VISIBLE
    }

    /**
     * Affiche les détails d'un bus
     */
    private fun showBusDetails(bus: BusPosition) {
        val message = buildString {
            append("Bus ${bus.busId}\n")
            append("Ligne: ${bus.ligne}\n")
            bus.destination?.let { dest -> append("Destination: $dest\n") }
            bus.minutesUntilArrival?.let { minutes ->
                append("Arrivée estimée: $minutes min\n")
            }
            bus.distanceToStop?.let { distance ->
                val distanceText = if (distance < 1000) {
                    "${distance.toInt()} m"
                } else {
                    "${String.format("%.1f", distance / 1000)} km"
                }
                append("Distance: $distanceText")
            }
        }

        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Observateur pour les données du ViewModel
     */
    private fun observeViewModel() {
        viewModel.selectedStop.observe(viewLifecycleOwner) { stop ->
            stop?.let {
                Log.d(TAG, "Station sélectionnée: ${it.stopName}")
                displayStopDetails(it)
                centerMapOnStop(it)
            }
        }

        viewModel.timeEstimation.observe(viewLifecycleOwner) { estimation ->
            estimation?.let {
                displayTimeEstimation(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    /**
     * Observateur pour les mises à jour de localisation
     */
    private fun observeLocation() {
        locationManager.locationPermissionGranted.observe(viewLifecycleOwner) { isGranted ->
            if (isGranted) {
                startLocationUpdates()
            }
        }

        locationManager.locationLiveData.observe(viewLifecycleOwner) { location ->
            location?.let {
                viewModel.selectedStop.value?.let { stop ->
                    viewModel.calculateLocalEstimation(it)
                }
            }
        }
    }

    /**
     * Charge les données de la station de bus
     */
    private fun loadStopData(stopId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Chargement des données pour station ID: $stopId")

                // Essayer d'abord de récupérer la station directement par son ID
                val stop = stopRepository.getStopById(stopId)

                if (stop != null) {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Station trouvée directement: ${stop.stopName}")
                        viewModel.setSelectedStop(stop)

                        locationManager.locationLiveData.value?.let { location ->
                            viewModel.calculateLocalEstimation(location)
                        }
                    }
                } else {
                    // Fallback: essayer de récupérer toutes les stations et filtrer
                    Log.d(TAG, "Station non trouvée directement, essai avec getAllStopsInMarrakech")
                    val stops = stopRepository.getAllStopsInMarrakech()

                    stops?.find { it.stopId == stopId }?.let { foundStop ->
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "Station trouvée via liste: ${foundStop.stopName}")
                            viewModel.setSelectedStop(foundStop)

                            locationManager.locationLiveData.value?.let { location ->
                                viewModel.calculateLocalEstimation(location)
                            }
                        }
                    } ?: run {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "Station avec ID $stopId non trouvée")
                            Snackbar.make(binding.root, "Station non trouvée", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Erreur chargement station: ${e.message}")
                    Snackbar.make(
                        binding.root,
                        "Erreur: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Appelé quand la carte est prête
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        checkLocationPermission()

        viewModel.selectedStop.value?.let { stop ->
            centerMapOnStop(stop)
        }
    }

    /**
     * Vérifie et demande les permissions de localisation
     */
    private fun checkLocationPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        locationManager.setPermissionStatus(permissionGranted)

        if (permissionGranted) {
            startLocationUpdates()
        }
    }

    /**
     * Démarre les mises à jour de localisation
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        map?.isMyLocationEnabled = true
        locationManager.startLocationUpdates()
        locationManager.getLastLocation()
    }

    /**
     * Affiche les détails de la station
     */
    private fun displayStopDetails(stop: Stop) {
        binding.tvStopName.text = stop.stopName
        binding.tvStopLocation.text = stop.ville
    }

    /**
     * Affiche l'estimation de temps et de distance
     */
    private fun displayTimeEstimation(estimation: TimeEstimation) {
        binding.tvDistanceLabel.text = getString(
            R.string.distance_meters,
            estimation.distance.toInt()
        )

        binding.tvWalkingTimeLabel.text = getString(
            R.string.walking_time_minutes,
            estimation.walkingTimeMinutes.toInt()
        )
    }

    /**
     * Centre la carte sur la station
     */
    private fun centerMapOnStop(stop: Stop) {
        map?.let { googleMap ->
            val latLng = LatLng(stop.latitude, stop.longitude)
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            )

            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(stop.stopName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
        }
    }
}
