package com.example.subscriber

import Subscriber
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    val pointsList = mutableListOf<CustomMarkerPoints>()
    private lateinit var mqttSubscriber: Subscriber
//    private var client: Mqtt5AsyncClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mqttSubscriber = Subscriber(
            brokerHost = "broker-816035889.sundaebytestt.com",
            port = 1883,
            topic = "assignment/location",
            onMessageReceived = { message ->
                Log.d("MainActivity", "Message received: $message")
                runOnUiThread { handleIncomingMessage(message) }
            }
        )
        mqttSubscriber.initializeCilent()
        mqttSubscriber.connectAndSubscribe()
    }

    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap
    }
    private fun addMarkerAtLocation(latLng: LatLng) {
        val newCustomPoint = CustomMarkerPoints(pointsList.size + 1, latLng)

        pointsList.add(newCustomPoint)

        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Marker ${newCustomPoint.id}")
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        drawPolyline()
    }
    private fun drawPolyline() {
        val latLngPoints = pointsList.map { it.point }

        val polylineOptions = PolylineOptions()
            .addAll(latLngPoints)
            .color(Color.BLUE)
            .width(5f)
            .geodesic(true)

        mMap.addPolyline(polylineOptions)

        val bounds = LatLngBounds.builder()
        latLngPoints.forEach { bounds.include(it) }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
    }
    private fun handleIncomingMessage(message: String) {


        val dataMap = message.split(", ").associate {
            val (key, value) = it.split(": ")
            key.trim() to value.trim()
        }

        val studentID = dataMap["StudentID"] ?: ""
        val timestamp = dataMap["Time"]?.toLongOrNull() ?: System.currentTimeMillis()
        val speed = dataMap["Speed"]?.replace(" km/h", "")?.toDoubleOrNull() ?: 0.0
        val latitude = dataMap["Latitude"]?.toDoubleOrNull() ?: 0.0
        val longitude = dataMap["Longitude"]?.toDoubleOrNull() ?: 0.0


        Log.d("MQTTSubscriber", "Data saved: $message")
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttSubscriber.disconnect()
    }
}
