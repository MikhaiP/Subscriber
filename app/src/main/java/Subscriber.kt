import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.subscriber.R
import java.nio.charset.StandardCharsets
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import java.util.UUID



class Subscriber : AppCompatActivity() {
    private var client: Mqtt5AsyncClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        client = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker-816035889.sundaebytestt.com")
            .serverPort(1883)
            .buildAsync()

        connectAndSubscribe()
    }

    fun connectAndSubscribe() {
        client?.connect()?.whenComplete { _, throwable ->
            if (throwable != null) {
                Log.e("MQTTSubscriber", "Connection failed: ${throwable.message}")
                Toast.makeText(this, "Failed to connect to broker", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("MQTTSubscriber", "Connected successfully")
                subscribeToTopic()
            }
        }
    }

    private fun subscribeToTopic() {
        client?.subscribeWith()
            ?.topicFilter("assignment/location")
            ?.callback { publish: Mqtt5Publish ->
                val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                Log.d("MQTTSubscriber", "Received message: $message")
                handleIncomingMessage(message)
            }
            ?.send()
            ?.whenComplete { _, throwable ->
                if (throwable != null) {
                    Log.e("MQTTSubscriber", "Subscription failed: ${throwable.message}")
                    Toast.makeText(this, "Failed to subscribe to topic", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("MQTTSubscriber", "Subscribed to topic successfully")
                    Toast.makeText(this, "Subscribed to topic", Toast.LENGTH_SHORT).show()
                }
            }
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
        client?.disconnect()?.whenComplete { _, _ -> Log.d("MQTTSubscriber", "Disconnected") }
    }
}