package com.example.cargame

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(R.layout.map_layout), OnMapReadyCallback {

    private lateinit var tv: TextView
    private var gMap: GoogleMap? = null
    private var marker: Marker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv = view.findViewById(R.id.tvMapHint)

        val mapFrag = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also { frag ->
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, frag)
                    .commit()
            }

        mapFrag.getMapAsync(this)

        parentFragmentManager.setFragmentResultListener("score_loc", viewLifecycleOwner) { _, b ->
            val name = b.getString("name").orEmpty()
            val score = b.getInt("score", 0)
            val lat = b.getDouble("lat", Double.NaN)
            val lng = b.getDouble("lng", Double.NaN)

            if (lat.isNaN() || lng.isNaN()) {
                tv.text = "No location for this score"
                marker?.remove()
                marker = null
                return@setFragmentResultListener
            }

            val map = gMap ?: run {
                tv.text = "Map is loading..."
                return@setFragmentResultListener
            }

            val pos = LatLng(lat, lng)
            tv.text = "Location: ${"%.5f".format(lat)}, ${"%.5f".format(lng)}"

            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title("$name ($score)")
            )

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map
        tv.text = "Map ready"

        map.setOnMapLoadedCallback {
            tv.text = "Map loaded"
        }
    }
}
