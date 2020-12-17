package com.example.golfcourses

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.infowindow.view.*
import org.json.JSONArray
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.N)
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        private val contents: View = layoutInflater.inflate(R.layout.infowindow, null)

        override fun getInfoWindow(marker: Marker?): View? {
            return null
        }

        override fun getInfoContents(marker: Marker): View {
            // UI elements
            val titleTextView = contents.titleTextView
            val addressTextView = contents.addressTextView
            val phoneTextView = contents.phoneTextView
            val emailTextView = contents.emailTextView
            val webTextView = contents.webTextView
            // title
            titleTextView.text = marker.title.toString()
            // get data from Tag list
            if (marker.tag is List<*>){
                val list: List<String> = marker.tag as List<String>
                addressTextView.text = list[0]
                phoneTextView.text = list[1]
                emailTextView.text = list[2]
                webTextView.text = list[3]
            }
            return contents
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        loadData(mMap)
    }

    private fun loadData(googleMap: GoogleMap) {
        mMap = googleMap

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        val url = "https://ptm.fi/materials/golfcourses/golf_courses.json"

        // Map different course types to different colors
        val courseTypes: Map<String, Float> = mapOf(
            "?" to BitmapDescriptorFactory.HUE_VIOLET,
            "Etu" to BitmapDescriptorFactory.HUE_BLUE,
            "Kulta" to BitmapDescriptorFactory.HUE_GREEN,
            "Kulta/Etu" to BitmapDescriptorFactory.HUE_YELLOW
        )

        var golfCourses: JSONArray

        // Request a string response from the provided URL.
        val jsonRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    // Extract the actual courses array from the response.
                    golfCourses = response.getJSONArray("courses")

                    // Add markers according to the data.
                    for (i in 0 until golfCourses.length()){
                        val course = golfCourses.getJSONObject(i)
                        val lat = course["lat"].toString().toDouble()
                        val lng = course["lng"].toString().toDouble()
                        val coord = LatLng(lat, lng)
                        val type = course["type"].toString()
                        val title = course["course"].toString()
                        val address = course["address"].toString()
                        val phone = course["phone"].toString()
                        val email = course["email"].toString()
                        val webUrl = course["web"].toString()

                        if (courseTypes.containsKey(type)){
                            val m = mMap.addMarker(
                                    MarkerOptions()
                                            .position(coord)
                                            .title(title)
                                            // Get the marker color by finding a match in the course types map.
                                            .icon(BitmapDescriptorFactory
                                                    .defaultMarker(courseTypes.getOrDefault(type, BitmapDescriptorFactory.HUE_RED)
                                                    )
                                            )
                            )

                            // Pass data to the marker via tags.
                            val list = listOf(address, phone, email, webUrl)
                            m.tag = list
                        } else {
                            Log.d("error", "This course type does not exist in evaluation: $type")
                        }
                    }
                },
                Response.ErrorListener { Log.d("requestError", "requestError") })

        // Add the request to the RequestQueue.
        queue.add(jsonRequest)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(65.5, 26.0),5.0F))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
    }
}