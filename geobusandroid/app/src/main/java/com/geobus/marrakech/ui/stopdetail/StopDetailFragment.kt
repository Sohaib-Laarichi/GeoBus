package com.geobus.marrakech.ui.stopdetail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.geobus.marrakech.R
import com.geobus.marrakech.databinding.FragmentStopDetailBinding
import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.model.TimeEstimation
import com.geobus.marrakech.repository.StopRepository
import com.geobus.marrakech.util.LocationManager
import com.geobus.marrakech.util.MapUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment pour afficher les détails d'une station
 */
class StopDetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentStopDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StopDetailViewModel
    private lateinit var locationManager: LocationManager

    private val args: StopDetailFragmentArgs by navArgs()
    private var map: GoogleMap? = null

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

        // Initialiser le ViewModel
        viewModel = ViewModelProvider(this)[StopDetailViewModel::class.java]

        // Initialiser le gestionnaire de localisation
        locationManager = LocationManager(requireContext())

        // Configurer la carte
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapPreview) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurer le bouton d'itinéraire
        binding.btnOpenMaps.setOnClickListener {
            viewModel.selectedStop.value?.let { stop ->
                openDirectionsInGoogleMaps(stop)
            }
        }

        // Observer les données
        observeViewModel()
        observeLocation()

        // Charger les données de la station
        loadStopData(args.stopId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationManager.stopLocationUpdates()
    }

    /**
     * Observateur pour les données du ViewModel
     */
    private fun observeViewModel() {
        // Observer la station sélectionnée
        viewModel.selectedStop.observe(viewLifecycleOwner) { stop ->
            stop?.let {
                displayStopDetails(it)
                centerMapOnStop(it)
            }
        }

        // Observer l'estimation de temps
        viewModel.timeEstimation.observe(viewLifecycleOwner) { estimation ->
            estimation?.let {
                displayTimeEstimation(it)
            }
        }

        // Observer l'état de chargement
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observer les erreurs
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
        // Observer l'état de la permission
        locationManager.locationPermissionGranted.observe(viewLifecycleOwner) { isGranted ->
            if (isGranted) {
                startLocationUpdates()
            }
        }

        // Observer les mises à jour de position
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
                val repository = StopRepository()
                val stops = repository.getAllStopsInMarrakech()

                stops?.find { it.stopId == stopId }?.let { stop ->
                    withContext(Dispatchers.Main) {
                        viewModel.setSelectedStop(stop)

                        // Si la localisation est disponible, calculer le temps de marche
                        locationManager.locationLiveData.value?.let { location ->
                            viewModel.calculateLocalEstimation(location)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
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

        // Configurer la carte
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        // Vérifier les permissions de localisation
        checkLocationPermission()

        // Centre la carte sur la station par défaut
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
        ) == PackageManager.PERMISSION_GRANTED

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
            MapUtils.centerMapOnLocation(googleMap, latLng, MapUtils.DETAIL_ZOOM)

            // Ajouter un marqueur pour la station
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(stop.stopName)
                .icon(MapUtils.getBitmapFromVector(requireContext(), R.drawable.ic_bus_stop))

            googleMap.addMarker(markerOptions)
        }
    }

    /**
     * Ouvre Google Maps avec l'itinéraire vers la station
     */
    private fun openDirectionsInGoogleMaps(stop: Stop) {
        // Obtenir la position actuelle de l'utilisateur si disponible
        val userLocation = locationManager.locationLiveData.value
        val originLatLng = if (userLocation != null) {
            "${userLocation.latitude},${userLocation.longitude}"
        } else {
            // Utiliser le centre de Marrakech comme position par défaut
            "31.6295,-7.9811"
        }

        // Construire l'URI pour l'itinéraire
        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&origin=$originLatLng" +
                    "&destination=${stop.latitude},${stop.longitude}" +
                    "&travelmode=walking" +
                    "&dir_action=navigate"
        )

        // Créer et lancer l'intent pour Google Maps
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback au navigateur si Google Maps n'est pas installé
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            try {
                // En cas d'erreur, essayer avec le navigateur web
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(browserIntent)
            } catch (e: Exception) {
                // Notifier l'utilisateur si aucune application ne peut ouvrir l'intent
                Snackbar.make(
                    binding.root,
                    "Impossible d'ouvrir l'itinéraire. Aucune application compatible.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}