package com.example.wropoznienia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var textInputDialog: AlertDialog
    private var enteredText = ""

    private lateinit var auth: FirebaseAuth
    private lateinit var filterButton: ImageButton
    private lateinit var statsButton: ImageButton
    private lateinit var filterRecyclerButton: ImageButton
    private lateinit var logoutButton: Button
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTextInputDialog()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setInfoWindowAdapter(this) // Set the custom info window adapter
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.1256586, 17.006079), 12.0f))
        var vehicleMap = HashMap<String, Marker>()
        var stopMap = HashMap<String, Marker>()
        val fileDownload = FileDownload()
        val context = this

        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logoutButton)
        currentUser = auth.currentUser!!
        if (currentUser == null) {
            val intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
            finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))  // Replace with your web client ID
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        logoutButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.signOut()

                    val intent = Intent(this@MainActivity, Login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Wylogowanie zakończone niepowodzeniem", Toast.LENGTH_SHORT).show()
                }
            }
        }

        statsButton = findViewById(R.id.buttonStatistics)
        statsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, Statistics::class.java)
            startActivity(intent)
        }

        filterRecyclerButton = findViewById(R.id.button_filter_recycler)
        filterRecyclerButton.setOnClickListener {
            val intent = Intent(this@MainActivity, VehiclesFilterRecycler::class.java)
            startActivity(intent)
        }


//        fileDownload.getFromFirestore(vehicleList, googleMap, db)
//
//        GlobalScope.launch {
//            while (isActive) {
//                delay(10_000)
//                runOnUiThread {
//                    vehicleList = fileDownload.updateMarkerDataOnce(vehicleList, googleMap, db)
//                }
//            }
//        }


        filterButton = findViewById(R.id.buttonClearFilter)
        filterButton.setOnClickListener {
            //textInputDialog.show()
            enteredText = ""
            Toast.makeText(context, "Wyczyszczono filter", Toast.LENGTH_SHORT).show()
        }

        enteredText = intent.getStringExtra("numer linii").toString()

        fileDownload.downloadFile(stopMap, stopMap, googleMap, application, context, enteredText, "stops.txt") { updatedVehicleMap ->
            stopMap = updatedVehicleMap
        }

        fileDownload.downloadFile(vehicleMap, stopMap, googleMap, application, context, enteredText, "vehicles_data.csv") { updatedVehicleMap ->
            vehicleMap = updatedVehicleMap
        }

        googleMap.setOnMarkerClickListener { marker ->
            if (stopMap.containsValue(marker)) {
                var newSnippet = "" //"Nadjeżdżające:\n"
                var totalDelay = 0
                var vehicleComingToStopCounter = 0
                for ((key, vehicle) in vehicleMap) {
                    var vehicleString = ""
                    try {
                        vehicleString = vehicle.tag as String
                        val vehicleInfo = vehicleString.split("&")
                        val stopsId = vehicleInfo[0].split("/")
                        val stopsNextStop = vehicleInfo[1]
                        val vehicleDelay = vehicleInfo[2].toDouble().roundToInt()
                        if (stopsId.contains(marker.tag)) {
                            if (stopsId.indexOf(marker.tag) >= stopsId.indexOf(stopsNextStop)) {
                                //newSnippet += vehicle.title + " " + vehicle.snippet + "\n"
                                totalDelay += vehicleDelay
                                vehicleComingToStopCounter += 1
                            }
                        }
                    } catch (e: NullPointerException) {
                        Log.e("Vehicle tag", "Tag is null")
                    }
                }
                newSnippet += "Całkowite opóźnienie nadjeżdżających pojazdów: " + totalDelay + "s\n"
                if (vehicleComingToStopCounter == 0) {
                    vehicleComingToStopCounter = 1
                }
                newSnippet += "Średnie opóźnienie nadjeżdżających pojazdów: " + (totalDelay/vehicleComingToStopCounter) + "s\n"
                marker.snippet = newSnippet
            }
            marker.showInfoWindow()

            // Return true to indicate that the event has been consumed
            true
        }

        GlobalScope.launch {
            while (isActive) {
                delay(5_000)
                runOnUiThread {
                    fileDownload.downloadFile(
                        vehicleMap,
                        stopMap,
                        googleMap,
                        application,
                        context,
                        enteredText,
                        "vehicles_data.csv"
                    ) { updatedVehicleMap ->
                        vehicleMap = updatedVehicleMap
                    }
                    if (enteredText == "" && googleMap.cameraPosition.zoom > 15) {
                        for ((key, marker) in stopMap) {
                            marker.isVisible = true
                        }
                    } else if (enteredText == "" && googleMap.cameraPosition.zoom <= 15) {
                        for ((key, marker) in stopMap) {
                            marker.isVisible = false
                        }
                    }
                }
            }
        }
    }
    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

    private fun initTextInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wpisz numer lub nazwę linii")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            enteredText = input.text.toString()
            // Do something with the entered text
            // For example, you can display it in a TextView or perform some other action
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        textInputDialog = builder.create()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Inflate your custom info window layout
        val view = layoutInflater.inflate(R.layout.custom_info_window, null)

        // Populate the views in your custom info window layout
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)

        titleTextView.text = marker.title
        descriptionTextView.text = marker.snippet

        return view
    }

    override fun getInfoContents(marker: Marker): View? {
        // Return null to use the default info window
        return null
    }

//    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
//        val permissionsToRequest = ArrayList<String>()
//        for (permission in permissions) {
//            if (ContextCompat.checkSelfPermission(this, permission)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Permission is not granted
//                permissionsToRequest.add(permission)
//            }
//        }
//        if (permissionsToRequest.size > 0) {
//            ActivityCompat.requestPermissions(
//                this,
//                permissionsToRequest.toTypedArray(),
//                REQUEST_PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }
}