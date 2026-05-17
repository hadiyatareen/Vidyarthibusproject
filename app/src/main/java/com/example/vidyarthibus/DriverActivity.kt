package com.example.vidyarthibus

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class DriverActivity : AppCompatActivity() {

    private lateinit var busSpinner: Spinner
    private lateinit var btnEmpty: Button
    private lateinit var btnModerate: Button
    private lateinit var btnFull: Button

    private val databaseUrl = "https://vidyarthibus-78a9d-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database = FirebaseDatabase.getInstance(databaseUrl)

    private val routeKeys = arrayOf("RouteA", "RouteB", "RouteC", "RouteD")
    private val routeNames = arrayOf(
        "Route A - Engineering Block",
        "Route B - Hostel",
        "Route C - City Center",
        "Route D - Railway Station"
    )
    private var selectedBusKey = "RouteA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        busSpinner = findViewById(R.id.busSpinner)
        btnEmpty = findViewById(R.id.btnEmpty)
        btnModerate = findViewById(R.id.btnModerate)
        btnFull = findViewById(R.id.btnFull)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, routeNames)
        busSpinner.adapter = adapter

        busSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBusKey = routeKeys[position]
                Toast.makeText(
                    this@DriverActivity,
                    "Selected: ${routeNames[position]}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnEmpty.setOnClickListener { updateStatus("EMPTY") }
        btnModerate.setOnClickListener { updateStatus("MODERATE") }
        btnFull.setOnClickListener { updateStatus("FULL") }
    }

    private fun updateStatus(status: String) {
        val ref = database.reference
            .child("busRoutes")
            .child(selectedBusKey)

        val updates = mapOf(
            "status" to status,
            "timestamp" to ServerValue.TIMESTAMP, // CRITICAL: This enables 15-min timeout

        )

        ref.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "✅ ${routeNames[routeKeys.indexOf(selectedBusKey)]} updated to $status",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "❌ Failed: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}