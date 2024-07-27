package com.example.projectuas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileReader

class FormLokasi : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var inputNamaLokas: EditText
    private lateinit var inputDeskripsiLokas: EditText
    private lateinit var simpanButton: Button
    private var currentLocation: GeoPoint? = null
    private var currentMarker: Marker? = null

    private lateinit var locationManager: LocationManager
    private var locationUpdateCount = 0  // Counter for location updates
    private val maxLocationUpdates = 3   // Maximum number of location updates
    private var isEditing: Boolean = false // Flag to check if in edit mode
    private var locationData: ListHead? = null // Data to edit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_form_lokasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        inputNamaLokas = view.findViewById(R.id.input_nama_lokas)
        inputDeskripsiLokas = view.findViewById(R.id.input_deskripsi_lokas)
        simpanButton = view.findViewById(R.id.simpan)

        // Initialize MapView
        val ctx = requireContext()
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Initialize LocationManager
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Load data from arguments
        arguments?.let {
            val namaLokasi = it.getString("nama_lokasi")
            val deskripsiLokasi = it.getString("deskripsi_lokasi")
            val koordinatLokasi = it.getString("koordinat_lokasi")

            if (namaLokasi != null) {
                isEditing = true
                locationData = ListHead(namaLokasi, deskripsiLokasi.orEmpty(), koordinatLokasi.orEmpty())
                inputNamaLokas.setText(namaLokasi)
                inputNamaLokas.isEnabled = false // Disable editing for name
            } else {
                isEditing = false
                inputNamaLokas.isEnabled = true // Enable editing for name
            }

            inputDeskripsiLokas.setText(deskripsiLokasi)

            koordinatLokasi?.let { coords ->
                val (lat, lon) = coords.split(", ").map { it.toDouble() }
                currentLocation = GeoPoint(lat, lon)
                mapView.controller.setCenter(currentLocation)
                mapView.controller.setZoom(17.0)
                currentMarker = Marker(mapView).apply {
                    position = currentLocation!!
                    title = "Selected Location"
                    mapView.overlays.add(this)
                }
            }
        }

        simpanButton.setOnClickListener {
            val namaLokas = inputNamaLokas.text.toString()
            val deskripsiLokas = inputDeskripsiLokas.text.toString()
            val koordinatLokas = currentLocation?.let { "${it.latitude}, ${it.longitude}" } ?: ""

            if (deskripsiLokas.isNotEmpty() && currentLocation != null) {
                if (isEditing) {
                    // Update the ListHead object
                    locationData?.let {
                        it.deskripsi_lokasi = deskripsiLokas
                        it.koordinat_lokasi = koordinatLokas

                        val fileName = "${it.nama_lokasi}.json"
                        val currentData = readDataFromInternalStorage(requireContext(), fileName).toMutableList()
                        val index = currentData.indexOfFirst { item -> item.nama_lokasi == it.nama_lokasi }
                        if (index != -1) {
                            currentData[index] = it
                            saveDataToInternalStorage(requireContext(), fileName, currentData)
                        }
                    }
                } else {
                    // Create a new ListHead object
                    val listHead = ListHead(namaLokas, deskripsiLokas, koordinatLokas)

                    // Save the ListHead data to internal storage
                    val fileName = "$namaLokas.json"
                    val currentData = readDataFromInternalStorage(requireContext(), fileName).toMutableList()
                    currentData.add(listHead)
                    saveDataToInternalStorage(requireContext(), fileName, currentData)
                }
                // Navigate back or show a success message
                transitionToHomeFragment()
            } else {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
            }
        }

        // Request location updates
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startLocationUpdates()
        }
    }
    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            p?.let {
                currentLocation = it
                // Remove previous marker if any
                currentMarker?.let { marker -> mapView.overlays.remove(marker) }

                // Add new marker
                currentMarker = Marker(mapView)
                currentMarker?.position = it
                currentMarker?.title = "Selected Location"
                mapView.overlays.add(currentMarker)

                mapView.controller.setCenter(it)
            }
            return true
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            return false
        }
    })

    private fun startLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (locationUpdateCount < maxLocationUpdates) {
                val userLocation = GeoPoint(location.latitude, location.longitude)
                currentLocation = userLocation
                mapView.controller.setCenter(userLocation)
                mapView.controller.setZoom(17.0)
                locationUpdateCount++

                // Stop location updates after reaching the max count
                if (locationUpdateCount >= maxLocationUpdates) {
                    locationManager.removeUpdates(this)
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDetach() {
        super.onDetach()
        mapView.onDetach()
        locationManager.removeUpdates(locationListener)
    }

    // Data class for storing location details
    data class ListHead(
        var nama_lokasi: String = "",
        var deskripsi_lokasi: String = "",
        var koordinat_lokasi: String = ""
    )

    private fun transitionToHomeFragment() {
        val homeFragment = HomeFragment()
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        fragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, homeFragment)
            ?.addToBackStack(null) // Optional: add to back stack if you want to allow back navigation
            ?.commit()
    }

    private fun saveDataToInternalStorage(context: Context, fileName: String, data: List<ListHead>) {
        val file = File(context.filesDir, fileName)
        val gson = Gson()
        file.writeText(gson.toJson(data))
    }

    private fun readDataFromInternalStorage(context: Context, fileName: String): List<ListHead> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            return emptyList()
        }

        val fileReader = FileReader(file)
        val gson = Gson()
        val dataType = object : TypeToken<List<ListHead>>() {}.type
        val data: List<ListHead> = gson.fromJson(fileReader, dataType)
        fileReader.close()

        return data
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

