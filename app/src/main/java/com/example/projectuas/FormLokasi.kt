package com.example.projectuas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class FormLokasi : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var inputNamaLokas: EditText
    private lateinit var inputDeskripsiLokas: EditText
    private lateinit var simpanButton: Button
    private var currentLocation: GeoPoint? = null
    private var currentMarker: Marker? = null

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

        // Inisialisasi MapView
        val ctx = requireContext()
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Set default location
        val defaultLocation = GeoPoint(-6.200000, 106.816666) // Jakarta
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(defaultLocation)

        // Set MapEventsOverlay untuk mendeteksi klik pada peta
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    currentLocation = it
                    // Hapus marker sebelumnya jika ada
                    currentMarker?.let { marker -> mapView.overlays.remove(marker) }

                    // Tambah marker baru
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

        mapView.overlays.add(mapEventsOverlay)

        simpanButton.setOnClickListener {
            val namaLokas = inputNamaLokas.text.toString()
            val deskripsiLokas = inputDeskripsiLokas.text.toString()

            if (namaLokas.isNotEmpty() && deskripsiLokas.isNotEmpty() && currentLocation != null) {
                val latitude = currentLocation!!.latitude
                val longitude = currentLocation!!.longitude
                Toast.makeText(requireContext(), "Lokasi disimpan: Lat: $latitude, Lng: $longitude", Toast.LENGTH_SHORT).show()
                // Simpan data ke database atau lakukan aksi lain
            } else {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
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
    }
}
