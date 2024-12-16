import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.travel_application.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapSelectionFragment : DialogFragment() {

    private lateinit var mapView: MapView


    private var onLocationSelectedListener: ((Double, Double) -> Unit)? = null

    fun setOnLocationSelectedListener(listener: (Double, Double) -> Unit) {
        onLocationSelectedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_selection, container, false)

        mapView = view.findViewById(R.id.mapView)


        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val locations = arguments?.getSerializable("locations") as? List<Pair<Double, Double>> ?: emptyList()

        if (locations.isNotEmpty()) {
            mapView.controller.setCenter(GeoPoint(locations[0].first, locations[0].second))
            mapView.controller.setZoom(6.0)

            val overlayManager = mapView.overlays
            locations.forEach { (lat, lon) ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(lat, lon)
                marker.setOnMarkerClickListener { _, _ ->
                    onLocationSelectedListener?.invoke(lat, lon)
                    dismiss()
                    true
                }
                overlayManager.add(marker)
            }
        }


        return view
    }

    companion object {
        fun newInstance(locations: List<Pair<Double, Double>>): MapSelectionFragment {
            val fragment = MapSelectionFragment()
            val args = Bundle()
            args.putSerializable("locations", ArrayList(locations))
            fragment.arguments = args
            return fragment
        }
    }
}
