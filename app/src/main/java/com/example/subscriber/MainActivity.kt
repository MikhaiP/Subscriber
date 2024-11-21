package com.example.subscriber

//import Subscriber
import DatabaseHelper
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.google.gson.Gson
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.nio.charset.StandardCharsets
import java.util.UUID

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    val pointsList = mutableListOf<CustomMarkerPoints>()
    private lateinit var databaseHelper: DatabaseHelper
    private var client: Mqtt5AsyncClient? = null
    data class LocationData(
        val StudentID: String,
        val Time: Long,
        val Latitude: Double,
        val Longitude: Double
    )
    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastTime: Long? = null



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

        initializeClient()
        databaseHelper = DatabaseHelper(this)


    }
    private fun initializeClient() {
        if (client == null){
            client = Mqtt5Client.builder()
                .identifier("Subscriber-816035889")
                .serverHost("broker-816035889.sundaebytestt.com")
                .serverPort(1883)
                .buildAsync()
            Log.d("MQTTSubscriber", "Client initialized")
            connectAndSubscribe()
        } else{
            Log.d("MQTTSubscriber", "Client already initialized")
        }
        subscribeToTopic()
    }
    private fun connectAndSubscribe() {
        client?.connect()
            ?.whenComplete { _, throwable ->
            if (throwable != null) {
                Log.e("MQTTSubscriber", "Connection failed: ${throwable.message}")
            } else {
                Log.d("MQTTSubscriber", "Connected successfully")
                //subscribeToTopic()
            }
        } ?: Log.e("MQTTSubscriber", "Client is null, cannot connect")
    }
    private fun subscribeToTopic() {
        client?.subscribeWith()
            ?.topicFilter("assignment/location")
            ?.callback { publish: Mqtt5Publish ->
                val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                Log.e("MQTTSubscriber", "Received message: $message")
                 handleIncomingMessage(message)
            }
            ?.send()
            ?.whenComplete { _, throwable ->
                if (throwable != null) {
                    runOnUiThread {
                        Toast.makeText(this, "Failed to subscribe to topic", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("MQTTSubscriber", "Subscription failed: ${throwable.message}")
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Subscribed to topic: 'assignment/location'", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("MQTTSubscriber", "Subscribed to topic successfully")
                }
            }?: Log.e("MQTTSubscriber", "Client is null, cannot connect")
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
    private fun calculateDistance(lat1: Double, lon1:Double, lat2: Double, lon2: Double): Double{
        val R = 6371000.0
        val latDis = Math.toRadians(lat2-lat1)
        val lonDis = Math.toRadians(lon2-lon1)
        val A = Math.sin(latDis / 2) * Math.sin(latDis / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDis / 2) * Math.sin(lonDis / 2)
        val B = 2 * Math.atan2(Math.sqrt(A),Math.sqrt(1-A))
        return R * B
    }
  private fun addStudentToScrollView(studentID: String, speedInfo: String) {
    val scrollViewContainer = findViewById<LinearLayout>(R.id.scrollViewContainer)

    // Create a new horizontal LinearLayout for each student
    val studentItemLayout = LinearLayout(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 8, 0, 8) // Space between items
        }
        orientation = LinearLayout.HORIZONTAL
        setPadding(16, 16, 16, 16)
        setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null)) // Background color for the item
    }

    // TextView for Student ID
    val studentIdTextView = TextView(this).apply {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        text = studentID
        setTextColor(resources.getColor(android.R.color.white, null)) // White text color
        textSize = 16f
    }

    // TextView for Speed Info
    val speedInfoTextView = TextView(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = 16
        }
        text = speedInfo
        setTextColor(resources.getColor(android.R.color.white, null))
        textSize = 14f
    }

    // Add the TextViews to the student item layout
    studentItemLayout.addView(studentIdTextView)
    studentItemLayout.addView(speedInfoTextView)

    // Add the student item layout to the ScrollView container
    scrollViewContainer.addView(studentItemLayout)
}

    private fun handleIncomingMessage(message: String) {


        try{

            val locationData = Gson().fromJson(message, LocationData::class.java)

            val latRec = locationData.Latitude
            val lonRec = locationData.Longitude
            val timeRec = locationData.Time
            val studentIDRec = locationData.StudentID

            var speed = 0.0
            if(lastLat != null && lastLon != null && lastTime != null){
                val distance = calculateDistance(lastLat!!,lastLon!!,latRec,lonRec)
                val timeDiff = (timeRec - lastTime!!) / 1000.0
                if(timeDiff > 0){
                    speed = distance / timeDiff
                }
            }

            lastLat = latRec
            lastLon = lonRec
            lastTime = timeRec


            val speedInfo = "Speed: %.2f km/h".format(speed)


            val location = LatLng(latRec,lonRec)

            // i keep getting null values when i run in UI thread using studentIDRec but when i do it like this, it
            // sends the null value across and the actual values within it
            runOnUiThread{

                addStudentToScrollView(locationData.StudentID, speedInfo)
                addMarkerAtLocation(location)
            }
//            runOnUiThread{
//                addStudentToScrollView(locationData.StudentID,speedInfo)
//            }



            val rowID = databaseHelper.insertLocationData(studentIDRec, timeRec, latRec,lonRec,speed)
            if(rowID != -1L){
                Log.d("Database", "Data saved in: Row ID = $rowID")
            } else{
                Log.e("Database", "Failed to save data to database")
            }

            // Access the fields of the LocationData object
            Log.d("MQTTSubscriberMain", "Received Data: StudentID=${locationData.StudentID}, Time=${locationData.Time}, Latitude=${locationData.Latitude}, Longitude=${locationData.Longitude}")
//            , ID=${locationData.ID} , Speed=${locationData.Speed}
        } catch (e:Exception){
            Log.e("MQTTSubscriberMain", "Failed to parse message as JSON: ${e.message}")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        client?.disconnect()?.whenComplete { _, _ -> Log.d("MQTTSubscriber", "Disconnected") }
    }
}
