package com.geobus.marrakech.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.geobus.marrakech.R
import com.geobus.marrakech.databinding.FragmentMapBinding
import com.geobus.marrakech.model.BusPosition
import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.ui.auth.AuthViewModel
import com.geobus.marrakech.util.LocationManager
import com.geobus.marrakech.util.MapUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

private const val TAG = "MapFragment"

/**
 * Fragment pour afficher la carte des stations de bus avec toutes les fonctionnalités
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MapViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var locationManager: LocationManager

    private var map: GoogleMap? = null
    private var userMarker: Marker? = null
    private var nearestStopMarker: Marker? = null
    private val stopMarkers = mutableListOf<Marker>()
    private val busMarkers = mutableListOf<Marker>()

    private var showBuses = true // État pour afficher/masquer les bus

    // Permission de localisation
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions.entries.all { it.value }
        locationManager.setPermissionStatus(locationGranted)

        if (locationGranted) {
            startLocationUpdates()
        } else {
            showLocationPermissionSnackbar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Fragment création de la vue")
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        // Enable options menu in this fragment
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Configuration du fragment")

        // Initialiser le ViewModel
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]

        // Initialiser le AuthViewModel
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        // Initialiser le gestionnaire de localisation
        locationManager = LocationManager(requireContext())

        // Configurer la carte
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurer l'interface
        setupUI()

        // Observer les données
        observeViewModel()
        observeLocation()
    }

    /**
     * Configuration de l'interface utilisateur
     */
    private fun setupUI() {
        // Bouton de localisation
        binding.fabMyLocation.setOnClickListener {
            Log.d(TAG, "Clic sur le bouton de localisation")
            centerOnUserLocation()
        }

        // Bouton d'itinéraire
        binding.btnDirections.setOnClickListener {
            val nearestStop = viewModel.nearestStop.value
            if (nearestStop == null) {
                Snackbar.make(binding.root, "Aucune station proche trouvée", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val location = locationManager.locationLiveData.value
            if (location != null) {
                openDirectionsInGoogleMaps(location.latitude, location.longitude, nearestStop)
            } else {
                openDirectionsInGoogleMaps(31.6295, -7.9811, nearestStop)
                Snackbar.make(binding.root, "Position non disponible, utilisation du centre de Marrakech", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Bouton pour afficher/masquer les bus
        binding.fabToggleBuses?.apply {
            // Définir le texte initial en fonction de l'état
            text = if (showBuses) getString(R.string.hide_buses) else getString(R.string.show_buses)
            setOnClickListener {
                toggleBusDisplay()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Fragment visible")
        // Démarrer le rafraîchissement des bus
        viewModel.startPeriodicRefresh()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Fragment en pause")
        // Arrêter le rafraîchissement pour économiser la batterie
        viewModel.stopPeriodicRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Nettoyage des ressources")
        viewModel.stopPeriodicRefresh()
        _binding = null
        locationManager.stopLocationUpdates()
    }

    /**
     * Appelé quand la carte est prête
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: Carte Google Maps prête")
        map = googleMap

        // Configurer la carte
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        // Centrer sur Marrakech par défaut
        MapUtils.centerMapOnLocation(googleMap, MapUtils.DEFAULT_MARRAKECH_LOCATION)

        // Configurer les callbacks
        googleMap.setOnMarkerClickListener(this)

        // Vérifier les permissions de localisation
        checkLocationPermission()

        // Charger les données
        viewModel.loadStops()
        viewModel.loadBusPositions()
    }

    /**
     * Observateur pour les données du ViewModel
     */
    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Configuration des observateurs")

        // Observer la liste des stations
        viewModel.stops.observe(viewLifecycleOwner) { stops ->
            Log.d(TAG, "Mise à jour des stations: ${stops.size} stations reçues")
            displayStopMarkers(stops)
        }

        // Observer la station la plus proche
        viewModel.nearestStop.observe(viewLifecycleOwner) { stop ->
            stop?.let {
                Log.d(TAG, "Nouvelle station la plus proche: ${it.stopName}")
                displayNearestStop(it)
            }
        }

        // Observer les positions des bus
        viewModel.busPositions.observe(viewLifecycleOwner) { buses ->
            Log.d(TAG, "Mise à jour des positions de bus: ${buses.size} bus")
            if (showBuses) {
                displayBusMarkers(buses)
            }
        }

        // Observer les bus pour une station spécifique
        viewModel.busesForStop.observe(viewLifecycleOwner) { buses ->
            Log.d(TAG, "Bus pour station: ${buses.size} bus")
            displayBusesForStopInfo(buses)
        }

        // Observer l'état de chargement
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "État de chargement: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observer les erreurs
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "Erreur reçue: $it")
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) {
                        viewModel.loadStops()
                        viewModel.loadBusPositions()
                    }
                    .show()

                viewModel.clearError()
            }
        }
    }

    /**
     * Observateur pour les mises à jour de localisation
     */
    private fun observeLocation() {
        // Observer l'état de la permission
        locationManager.locationPermissionGranted.observe(viewLifecycleOwner) { isGranted ->
            Log.d(TAG, "Permission de localisation: $isGranted")
            if (isGranted) {
                startLocationUpdates()
            }
        }

        // Observer les mises à jour de position
        locationManager.locationLiveData.observe(viewLifecycleOwner) { location ->
            location?.let {
                Log.d(TAG, "Nouvelle position: ${it.latitude}, ${it.longitude}")
                updateUserLocationOnMap(it)
                viewModel.findNearestStopLocally(it)
            }
        }
    }

    /**
     * Vérifie et demande les permissions de localisation
     */
    private fun checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission: Vérification des permissions")
        if (hasLocationPermission()) {
            Log.d(TAG, "Permission de localisation déjà accordée")
            locationManager.setPermissionStatus(true)
        } else {
            Log.d(TAG, "Demande de permission de localisation")
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showLocationPermissionSnackbar() {
        Snackbar.make(
            binding.root,
            R.string.location_permission_required,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.grant_permission) {
            requestLocationPermission()
        }.show()
    }

    /**
     * Démarre les mises à jour de localisation
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: Démarrage des mises à jour de localisation")
        map?.isMyLocationEnabled = true
        locationManager.startLocationUpdates()
        locationManager.getLastLocation()
    }

    /**
     * Centre la carte sur la position de l'utilisateur
     */
    private fun centerOnUserLocation() {
        locationManager.locationLiveData.value?.let { location ->
            Log.d(TAG, "Centrage sur la position de l'utilisateur")
            map?.let { googleMap ->
                MapUtils.centerMapOnLocation(
                    googleMap,
                    LatLng(location.latitude, location.longitude),
                    MapUtils.DETAIL_ZOOM
                )
            }
        } ?: run {
            Log.w(TAG, "Position de l'utilisateur non disponible pour le centrage")
            Snackbar.make(
                binding.root,
                R.string.location_not_available,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Met à jour le marqueur de l'utilisateur sur la carte
     */
    private fun updateUserLocationOnMap(location: android.location.Location) {
        map?.let { googleMap ->
            userMarker?.remove()
            userMarker = MapUtils.addUserLocationMarker(requireContext(), googleMap, location)
        }
    }

    /**
     * Affiche les marqueurs des stations de bus sur la carte
     */
    private fun displayStopMarkers(stops: List<Stop>) {
        Log.d(TAG, "displayStopMarkers: Affichage de ${stops.size} marqueurs de stations")
        map?.let { googleMap ->
            // Effacer les marqueurs existants
            stopMarkers.forEach { it.remove() }
            stopMarkers.clear()

            // Ajouter les nouveaux marqueurs
            stopMarkers.addAll(
                MapUtils.addStopMarkers(requireContext(), googleMap, stops)
            )
        }
    }

    /**
     * Affiche les marqueurs des bus sur la carte
     */
    private fun displayBusMarkers(buses: List<BusPosition>) {
        Log.d(TAG, "displayBusMarkers: Affichage de ${buses.size} marqueurs de bus")
        map?.let { googleMap ->
            // Effacer les marqueurs de bus existants
            busMarkers.forEach { it.remove() }
            busMarkers.clear()

            // Ajouter les nouveaux marqueurs de bus
            busMarkers.addAll(
                MapUtils.addBusMarkers(requireContext(), googleMap, buses)
            )
        }
    }

    /**
     * Bascule l'affichage des bus
     */
    private fun toggleBusDisplay() {
        showBuses = !showBuses
        if (showBuses) {
            // Réafficher les bus
            viewModel.busPositions.value?.let { buses ->
                displayBusMarkers(buses)
            }
            binding.fabToggleBuses?.apply {
                setIconResource(R.drawable.ic_bus_visible)
                text = getString(R.string.hide_buses)
            }
        } else {
            // Masquer les bus
            busMarkers.forEach { it.remove() }
            busMarkers.clear()
            binding.fabToggleBuses?.apply {
                setIconResource(R.drawable.ic_bus_hidden)
                text = getString(R.string.show_buses)
            }
        }
    }

    /**
     * Affiche les informations sur la station la plus proche
     */
    private fun displayNearestStop(stop: Stop) {
        Log.d(TAG, "displayNearestStop: Station la plus proche: ${stop.stopName}")

        // Mettre à jour le marqueur sur la carte
        map?.let { googleMap ->
            nearestStopMarker?.remove()
            nearestStopMarker = MapUtils.addNearestStopMarker(requireContext(), googleMap, stop)
        }

        // Mettre à jour la carte d'informations
        binding.cardNearestStop.visibility = View.VISIBLE
        binding.tvNearestStopTitle.text = stop.stopName

        stop.distance?.let { distance ->
            binding.tvDistance.text = getString(
                R.string.distance_meters,
                distance.toInt()
            )
            Log.d(TAG, "Distance vers la station: ${distance.toInt()} m")
        }

        stop.walkingTimeMinutes?.let { walkingTime ->
            binding.tvWalkingTime.text = getString(
                R.string.walking_time_minutes,
                walkingTime.toInt()
            )
            Log.d(TAG, "Temps de marche estimé: ${walkingTime.toInt()} min")
        }

        // Rendre le bouton visible et s'assurer qu'il est cliquable
        binding.btnDirections.apply {
            visibility = View.VISIBLE
            isEnabled = true
            isClickable = true
        }

        // Charger les bus pour cette station
        viewModel.loadBusesForStop(stop.stopId)
    }

    /**
     * Affiche les informations sur les bus se dirigeant vers une station
     */
    private fun displayBusesForStopInfo(buses: List<BusPosition>) {
        if (buses.isEmpty()) {
            Log.d(TAG, "Aucun bus trouvé pour cette station")
            return
        }

        // Trier les bus par temps d'arrivée estimé
        val sortedBuses = buses.sortedBy { it.minutesUntilArrival ?: Int.MAX_VALUE }

        // Afficher les informations du prochain bus
        val nextBus = sortedBuses.firstOrNull()
        nextBus?.let { bus ->
            // Vous pouvez ajouter des éléments UI pour afficher ces informations
            Log.d(TAG, "Prochain bus: ${bus.busId} - Ligne ${bus.ligne}")
            bus.minutesUntilArrival?.let { minutes ->
                Log.d(TAG, "Arrivée dans: $minutes minutes")
                // Afficher dans un TextView si vous en ajoutez un
                // binding.tvNextBusInfo.text = "Prochain bus: Ligne ${bus.ligne} dans $minutes min"
            }
        }
    }

    /**
     * Handler pour le clic sur un marqueur
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        // Ignorer le marqueur de l'utilisateur
        if (marker == userMarker) {
            Log.d(TAG, "Clic sur le marqueur de l'utilisateur, ignoré")
            return false
        }

        // Vérifier si c'est un marqueur de bus
        val busId = marker.tag as? String
        if (busId != null) {
            Log.d(TAG, "Clic sur le marqueur du bus $busId")
            // Afficher les informations du bus
            showBusInfo(busId)
            return true
        }

        // Récupérer l'ID de la station
        val stopId = marker.tag as? Long
        if (stopId == null) {
            Log.e(TAG, "Clic sur marqueur sans ID de station")
            return false
        }

        Log.d(TAG, "Clic sur le marqueur de la station $stopId")

        // Trouver la station correspondante
        val stop = viewModel.stops.value?.find { it.stopId == stopId }
        if (stop == null) {
            Log.e(TAG, "Station avec ID $stopId non trouvée dans la liste")
            return false
        }

        // Naviguer vers le détail de la station
        Log.d(TAG, "Navigation vers le détail de la station ${stop.stopName}")
        findNavController().navigate(
            MapFragmentDirections.actionMapFragmentToStopDetailFragment(stopId)
        )

        return true
    }

    /**
     * Affiche les informations d'un bus
     */
    private fun showBusInfo(busId: String) {
        val bus = viewModel.busPositions.value?.find { it.busId == busId }
        bus?.let {
            val message = buildString {
                append("Bus ${it.busId}\n")
                append("Ligne: ${it.ligne}\n")
                it.destination?.let { dest -> append("Destination: $dest\n") }
                it.minutesUntilArrival?.let { minutes ->
                    append("Arrivée dans: $minutes min")
                }
            }

            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Ouvre Google Maps avec l'itinéraire vers la station
     */
    private fun openDirectionsInGoogleMaps(userLat: Double, userLon: Double, stop: Stop) {
        Log.d(TAG, "openDirectionsInGoogleMaps: Ouverture de Google Maps avec itinéraire vers ${stop.stopName}")

        try {
            // Construire l'URI pour l'itinéraire
            val uri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&origin=$userLat,$userLon" +
                        "&destination=${stop.latitude},${stop.longitude}" +
                        "&travelmode=walking" +
                        "&dir_action=navigate"
            )

            Log.d(TAG, "URI de navigation: $uri")

            // Créer l'intent pour ouvrir Google Maps
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // Essayer de spécifier Google Maps
            intent.setPackage("com.google.android.apps.maps")

            // Vérifier si Google Maps est disponible
            val packageManager = requireContext().packageManager
            val activities = packageManager.queryIntentActivities(intent, 0)

            val isGoogleMapsInstalled = activities.any {
                it.activityInfo.packageName == "com.google.android.apps.maps"
            }

            if (isGoogleMapsInstalled) {
                Log.d(TAG, "Google Maps est installé, ouverture avec l'app Google Maps")
                startActivity(intent)
            } else {
                Log.w(TAG, "Google Maps n'est pas installé, ouverture avec le navigateur")
                // Fallback: ouvrir avec n'importe quelle application qui peut gérer les URI geo
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(browserIntent)
            }

            Log.d(TAG, "Intent envoyé avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ouverture de Google Maps: ${e.message}", e)

            try {
                // Tentative de repli sur le navigateur web général
                Log.d(TAG, "Tentative d'ouverture avec le navigateur web")
                val browserUri = Uri.parse(
                    "https://www.google.com/maps/dir/$userLat,$userLon/${stop.latitude},${stop.longitude}/data=!3m1!4b1!4m2!4m1!3e2"
                )
                val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                startActivity(browserIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Échec de l'ouverture avec le navigateur: ${e2.message}", e2)

                // Afficher un message d'erreur
                Snackbar.make(
                    binding.root,
                    "Impossible d'ouvrir l'itinéraire. Vérifiez que Google Maps ou un navigateur web est installé.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Inflate the options menu
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Handle menu item selection
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Logout the user and navigate to login screen
     */
    private fun logout() {
        Log.d(TAG, "logout: Déconnexion de l'utilisateur")

        // Call logout in AuthViewModel
        authViewModel.logout()

        // Show confirmation message
        Snackbar.make(
            binding.root,
            "Vous avez été déconnecté avec succès",
            Snackbar.LENGTH_SHORT
        ).show()

        // Navigate to login screen
        findNavController().navigate(R.id.loginFragment)
    }
}
