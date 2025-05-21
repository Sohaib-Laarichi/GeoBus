package com.geobus.marrakech.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.geobus.marrakech.R
import com.geobus.marrakech.databinding.FragmentMapBinding
import com.geobus.marrakech.model.Stop
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
 * Fragment pour afficher la carte des stations de bus
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MapViewModel
    private lateinit var locationManager: LocationManager

    private var map: GoogleMap? = null
    private var userMarker: Marker? = null
    private var nearestStopMarker: Marker? = null
    private val stopMarkers = mutableListOf<Marker>()

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Configuration du fragment")

        // Initialiser le ViewModel
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]

        // Initialiser le gestionnaire de localisation
        locationManager = LocationManager(requireContext())

        // Configurer la carte
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurer le bouton de localisation
        binding.fabMyLocation.setOnClickListener {
            Log.d(TAG, "Clic sur le bouton de localisation")
            centerOnUserLocation()
        }

        // Configurer le bouton d'itinéraire
        configureDirectionsButton()

        // Observer les données
        observeViewModel()
        observeLocation()
    }

    /**
     * Configurer correctement le bouton d'itinéraire
     */
    private fun configureDirectionsButton() {
        try {
            Log.d(TAG, "Configuration du bouton d'itinéraire")

            // S'assurer que le bouton est visible et cliquable
            binding.btnDirections.apply {
                visibility = View.VISIBLE
                isClickable = true
                isFocusable = true

                // Définir l'apparence du bouton pour qu'il ressemble à celui de la capture d'écran
                setBackgroundResource(R.drawable.button_directions_background)

                // Régler la marge si nécessaire
                val params = layoutParams as? ViewGroup.MarginLayoutParams
                params?.setMargins(0, 16, 0, 0)
                layoutParams = params

                // Définir le listener de clic avec un retour visuel
                setOnClickListener { view ->
                    Log.d(TAG, "Clic sur le bouton d'itinéraire détecté")
                    view.isPressed = true  // Effet visuel

                    try {
                        val nearestStop = viewModel.nearestStop.value
                        if (nearestStop == null) {
                            Log.e(TAG, "Aucune station proche trouvée")
                            Snackbar.make(
                                binding.root,
                                "Aucune station proche n'a été trouvée",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            view.isPressed = false
                            return@setOnClickListener
                        }

                        Log.d(TAG, "Station trouvée: ${nearestStop.stopName}, id=${nearestStop.stopId}")

                        // Vérifier la localisation
                        val location = locationManager.locationLiveData.value
                        if (location != null) {
                            Log.d(TAG, "Localisation disponible: ${location.latitude}, ${location.longitude}")
                            openDirectionsInGoogleMaps(location.latitude, location.longitude, nearestStop)
                        } else {
                            Log.w(TAG, "Localisation non disponible, utilisation de coordonnées par défaut")
                            // Si la localisation n'est pas disponible, utiliser le centre de Marrakech comme position par défaut
                            openDirectionsInGoogleMaps(31.6295, -7.9811, nearestStop)

                            // Avertir l'utilisateur que sa position exacte n'est pas disponible
                            Snackbar.make(
                                binding.root,
                                R.string.location_not_available,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "ERREUR lors du clic sur le bouton d'itinéraire: ${e.message}", e)

                        // Afficher un message d'erreur
                        Snackbar.make(
                            binding.root,
                            "Erreur lors de l'ouverture de l'itinéraire: ${e.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } finally {
                        view.isPressed = false  // Remettre à l'état normal
                    }
                }
            }

            Log.d(TAG, "Configuration du bouton d'itinéraire terminée avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "ERREUR grave lors de la configuration du bouton d'itinéraire: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Fragment visible")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Nettoyage des ressources")
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

        // Charger les stations de bus
        viewModel.loadStops()
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

        // Forcer un rafraîchissement du layout
        binding.root.invalidate()
        binding.cardNearestStop.invalidate()
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
}