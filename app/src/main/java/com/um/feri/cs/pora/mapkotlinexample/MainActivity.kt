package com.um.feri.cs.pora.mapkotlinexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.um.feri.cs.pora.mapkotlinexample.databinding.ActivityMainBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import java.util.*

class MainActivity : AppCompatActivity() {
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient //https://developer.android.com/training/location/retrieve-current
    private var lastLoction: Location? = null
    private var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var requestingLocationUpdates=false
    init {

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update UI with location data
                    updateLocation(location) //MY function
                }
            }
        }

        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if (allAreGranted) {
                initMap()
            }
        }
    }

    private lateinit var binding: ActivityMainBinding
    val rnd = Random()
    lateinit var map: MapView
    var startPoint: GeoPoint = GeoPoint(46.55951, 15.63970);
    lateinit var mapController: IMapController
    var marker: Marker? = null
    var path1: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance()
            .load(applicationContext, this.getPreferences(Context.MODE_PRIVATE))
        binding = ActivityMainBinding.inflate(layoutInflater) //ADD THIS LINE

        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        setContentView(binding.root)
        val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        activityResultLauncher.launch(appPerms)
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (requestingLocationUpdates) {
            requestingLocationUpdates = false
            stopLocationUpdates()
        }
        binding.map.onPause()
    }

    fun initLoaction() { //call in create
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        readLastKnownLocation()
    }
    private fun stopLocationUpdates() { //onPause
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() { //onResume
        locationRequest = LocationRequest.create().apply { //https://stackoverflow.com/questions/66489605/is-constructor-locationrequest-deprecated-in-google-maps-v2
            interval = 1000 //can be much higher
            fastestInterval = 500
            smallestDisplacement = 10f //10m
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 1000
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    fun updateLocation(newLocation: Location) {
        lastLoction = newLocation
        //GUI TODO
        binding.tvLat.setText(newLocation.latitude.toString())
        binding.tvLon.setText(newLocation.longitude.toString())
        //var currentPoint: GeoPoint = GeoPoint(newLocation.latitude, newLocation.longitude);
        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint
        map.invalidate()

    }

    //https://developer.android.com/training/location/retrieve-current
    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }

    fun initMap() {
        initLoaction()
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
        mapController.setZoom(18.5)
        mapController.setCenter(startPoint);
        map.invalidate()
    }


    private fun getPath(): Polyline { //Singelton
        if (path1 == null) {
            path1 = Polyline()
            path1!!.outlinePaint.color = Color.RED
            path1!!.outlinePaint.strokeWidth = 10f
            path1!!.addPoint(startPoint.clone())
            map.overlayManager.add(path1)
        }
        return path1!!
    }

    private fun getPositionMarker(): Marker { //Singelton
        if (marker == null) {
            marker = Marker(map)
            marker!!.title = "Here I am"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_position);
            map.overlays.add(marker)
        }
        return marker!!
    }


    fun onClickDraw1(view: View?) {
        startPoint.latitude = startPoint.latitude + (rnd.nextDouble() - 0.5) * 0.001
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint
        map.invalidate()
    }

    fun onClickDraw2(view: View?) {
        startPoint.latitude = startPoint.latitude + (rnd.nextDouble() - 0.5) * 0.001
        mapController.setCenter(startPoint)
        val circle = Polygon(map)
        circle.points = Polygon.pointsAsCircle(startPoint, 40.0 + rnd.nextInt(100))
        circle.fillPaint.color = 0x32323232 //transparent
        circle.outlinePaint.color = Color.GREEN
        circle.outlinePaint.strokeWidth = 2f
        circle.title = "Area X"
        map.overlays.add(circle) //Duplicate every time new
        map.invalidate()
    }

    fun onClickDraw3(view: View?) {
        val mCompassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
        mCompassOverlay.enableCompass()
        map.overlays.add(mCompassOverlay)
        map.invalidate()
    }

    fun onClickDraw4(view: View?) {
        //Polyline path = new Polyline();
        startPoint.latitude = startPoint.latitude + (rnd.nextDouble() - 0.5) * 0.001
        startPoint.longitude = startPoint.longitude + (rnd.nextDouble() - 0.5) * 0.001
        getPath().addPoint(startPoint.clone())
        map.invalidate()
    }
}

