//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.subscriber.MainActivity.LocationData
//import com.example.subscriber.R
//import com.google.gson.Gson
//import java.nio.charset.StandardCharsets
//import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
//import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
//import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
//import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
//import java.util.UUID
//
//
//
//class Subscriber(
//    private val brokerHost: String,
//    private val port: Int,
//    private val topic: String,
//    private val onMessageReceived: (String) -> Unit
//)  {
//    private var client: Mqtt5AsyncClient? = null
//
//    fun initializeCilent() {
//        if (client == null){
//            client = Mqtt5Client.builder()
//                .identifier(UUID.randomUUID().toString())
//                .serverHost("broker-816035889.sundaebytestt.com")
//                .serverPort(1883)
//                .buildAsync()
//        Log.d("MQTTSubscriber", "Client initialized")
//        } else{
//            Log.d("MQTTSubscriber", "Client already initialized")
//        }
//
//    }
//
//    fun connectAndSubscribe() {
//        client?.connect()?.whenComplete { _, throwable ->
//            if (throwable != null) {
//                Log.e("MQTTSubscriber", "Connection failed: ${throwable.message}")
//            } else {
//                Log.d("MQTTSubscriber", "Connected successfully")
//                subscribeToTopic()
//            }
//        } ?: Log.e("MQTTSubscriber", "Client is null, cannot connect")
//    }
//
//    private fun subscribeToTopic() {
//        client?.subscribeWith()
//            ?.topicFilter("assignment/location")
//            ?.callback { publish: Mqtt5Publish ->
//                val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
//                Log.e("MQTTSubscriberFF", "Received message: $message")
//                handleIncomingMessage(message)
//            }
//            ?.send()
//            ?.whenComplete { _, throwable ->
//                if (throwable != null) {
//                    Log.e("MQTTSubscriber", "Subscription failed: ${throwable.message}")
//                } else {
//                    Log.d("MQTTSubscriber", "Subscribed to topic successfully")
//                }
//            }?: Log.e("MQTTSubscriber", "Client is null, cannot connect")
//    }
//
//    private fun handleIncomingMessage(message: String) {
//
//
////        val dataMap = message.split(", ").associate {
////            val (key, value) = it.split(": ")
////            key.trim() to value.trim()
////        }
////
////        val studentID = dataMap["StudentID"] ?: ""
////        val timestamp = dataMap["Time"]?.toLongOrNull() ?: System.currentTimeMillis()
////        val speed = dataMap["Speed"]?.replace(" km/h", "")?.toDoubleOrNull() ?: 0.0
////        val latitude = dataMap["Latitude"]?.toDoubleOrNull() ?: 0.0
////        val longitude = dataMap["Longitude"]?.toDoubleOrNull() ?: 0.0
//        try{
//            val gson = Gson()
//
//            val locationData = gson.fromJson(message, LocationData::class.java)
//
//            // Access the fields of the LocationData object
//            Log.d("MQTTSubscriber", "Received Data: StudentID=${locationData.StudentID}, Time=${locationData.Time}, Latitude=${locationData.Latitude}, Longitude=${locationData.Longitude}")
////            , ID=${locationData.ID} , Speed=${locationData.Speed}
//        } catch (e:Exception){
//            Log.e("MQTTSubscriber", "Failed to parse message as JSON: ${e.message}")
//        }
//
//        Log.d("MQTTSubscriber", "Data saved: $message")
//    }
//
////    override fun onDestroy() {
////        super.onDestroy()
////        client?.disconnect()?.whenComplete { _, _ -> Log.d("MQTTSubscriber", "Disconnected") }
////    }
//fun disconnect() {
//    client?.disconnect()?.whenComplete { _, _ -> Log.d("MQTTSubscriber", "Disconnected") }
//}
//}