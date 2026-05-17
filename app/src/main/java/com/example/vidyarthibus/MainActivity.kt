package com.example.vidyarthibus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var busSpinner: Spinner
    private lateinit var checkButton: Button
    private lateinit var backButton: Button // NEW

    private lateinit var statusText: TextView
    private lateinit var crowdProgressBar: ProgressBar
    private lateinit var busInfoText: TextView
    private lateinit var driverText: TextView

    private lateinit var selectBusLayout: View
    private lateinit var crowdLayout: View
    private lateinit var sharedAutoLayout: View
    private lateinit var autoContact1: TextView
    private lateinit var autoContact2: TextView

    private var selectedBusKey: String = "RouteA"
    private val databaseUrl = "https://vidyarthibus-78a9d-default-rtdb.asia-southeast1.firebasedatabase.app"
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance(databaseUrl)

        busSpinner = findViewById(R.id.busSpinner)
        checkButton = findViewById(R.id.checkButton)
        backButton = findViewById(R.id.backButton) // NEW

        statusText = findViewById(R.id.statusText)
        crowdProgressBar = findViewById(R.id.crowdProgressBar)
        busInfoText = findViewById(R.id.busInfoText)
        driverText = findViewById(R.id.driverText)

        selectBusLayout = findViewById(R.id.selectBusLayout)
        crowdLayout = findViewById(R.id.crowdLayout)
        sharedAutoLayout = findViewById(R.id.sharedAutoLayout)
        autoContact1 = findViewById(R.id.autoContact1)
        autoContact2 = findViewById(R.id.autoContact2)

        val busList = arrayOf(
            "Route A - Engineering Block",
            "Route B - Hostel",
            "Route C - City Center",
            "Route D - Railway Station"
        )

        busSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, busList)

        busSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBusKey = when (position) {
                    0 -> "RouteA"
                    1 -> "RouteB"
                    2 -> "RouteC"
                    3 -> "RouteD"
                    else -> "RouteA"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        checkButton.setOnClickListener {
            selectBusLayout.visibility = View.GONE
            crowdLayout.visibility = View.VISIBLE
            statusText.text = "Loading..."
            listenToBusStatus(selectedBusKey)
        }

        // NEW: Back button logic
        backButton.setOnClickListener {
            crowdLayout.visibility = View.GONE
            selectBusLayout.visibility = View.VISIBLE
        }

        autoContact1.setOnClickListener { dialNumber("9876543210") }
        autoContact2.setOnClickListener { dialNumber("8765432109") }
    }

    // NEW: Handle phone back button
    override fun onBackPressed() {
        if (crowdLayout.visibility == View.VISIBLE) {
            crowdLayout.visibility = View.GONE
            selectBusLayout.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    private fun listenToBusStatus(bus: String) {
        database.reference.child("busRoutes").child(bus)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        statusText.text = "No data found for $bus"
                        return
                    }

                    val status = snapshot.child("status").getValue(String::class.java)
                    val busNumber = snapshot.child("busNumber").getValue(String::class.java)
                    val driverName = snapshot.child("driverName").getValue(String::class.java)
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                    busInfoText.text = "Bus No: ${busNumber ?: "NA"}"
                    driverText.text = "Driver: ${driverName ?: "NA"}"

                    val currentTime = System.currentTimeMillis()
                    val fifteenMinutes = 15 * 60 * 1000L

                    if (timestamp == 0L || currentTime - timestamp > fifteenMinutes) {
                        statusText.text = "⚠️ No recent updates"
                        crowdProgressBar.progress = 0
                        statusText.setBackgroundColor(0xFF9E9E9E.toInt())
                        sharedAutoLayout.visibility = View.GONE
                        return
                    }

                    when (status?.uppercase()) {
                        "EMPTY" -> {
                            statusText.text = "🟢 EMPTY - Seats available"
                            crowdProgressBar.progress = 20
                            statusText.setBackgroundColor(0xFF4CAF50.toInt())
                            sharedAutoLayout.visibility = View.GONE
                        }
                        "MODERATE" -> {
                            statusText.text = "🟡 MODERATE - Few seats"
                            crowdProgressBar.progress = 60
                            statusText.setBackgroundColor(0xFFFFC107.toInt())
                            sharedAutoLayout.visibility = View.GONE
                        }
                        "FULL" -> {
                            statusText.text = "🔴 FULL - No seats"
                            crowdProgressBar.progress = 100
                            statusText.setBackgroundColor(0xFFF44336.toInt())
                            sharedAutoLayout.visibility = View.VISIBLE
                        }
                        else -> {
                            statusText.text = "Status: $status"
                            crowdProgressBar.progress = 0
                            sharedAutoLayout.visibility = View.GONE
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    statusText.text = "Error: ${error.message}"
                }
            })
    }

    private fun dialNumber(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }
}