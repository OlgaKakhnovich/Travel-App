package com.example.travel_application

import android.Manifest
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.travel_application.databinding.FragmentMapBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.HttpURLConnection
import java.net.URL

class MapFragment : Fragment(), MapListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            requireContext(),
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)
        controller = mMap.controller

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            enableLocation()
        }

        loadPlaces()

        return binding.root
    }

    private fun enableLocation() {
        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mMap)
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        mMyLocationOverlay.runOnFirstFix {
            activity?.runOnUiThread {
                val myLocation = mMyLocationOverlay.myLocation
                if (myLocation != null) {
                    controller.setZoom(4.0)
                    controller.setCenter(myLocation)
                }
            }
        }

        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)
    }

    override fun onScroll(event: ScrollEvent?): Boolean = true
    override fun onZoom(event: ZoomEvent?): Boolean = true

    override fun onPause() {
        super.onPause()
        mMyLocationOverlay.disableMyLocation()
    }

    override fun onResume() {
        super.onResume()
        mMyLocationOverlay.enableMyLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun loadPlaces() {
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid ?: return

        firestore.collection("places")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    val title = "${document.getString("city")}, ${document.getString("countryCode")}"
                    val rating = document.getDouble("rating")?.toInt() ?: 0

                    if (latitude != null && longitude != null) {
                        addMarker(latitude, longitude, title ?: "Miejsce", rating)
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    private fun addMarker(lat: Double, lon: Double, title: String, rating: Int) {
        val marker = Marker(mMap)
        marker.position = GeoPoint(lat, lon)
        marker.title = title
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        when (rating) {
            1 -> marker.icon = resources.getDrawable(R.drawable.pin1orange, null)
            2 -> marker.icon = resources.getDrawable(R.drawable.pin2mediumorange, null)
            3 -> marker.icon = resources.getDrawable(R.drawable.pin3lightorange, null)
            4 -> marker.icon = resources.getDrawable(R.drawable.pin4yellow, null)
            5 -> marker.icon = resources.getDrawable(R.drawable.pin5blue, null)
            else -> marker.icon = resources.getDrawable(R.drawable.add_location_icon, null)
        }


        mMap.overlays.add(marker)
    }
}
