package com.pnpawd.userapp.pipeinstallation.Submitted

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.R
import com.pnpawd.userapp.pipeinstallation.LandInfoPreviewActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class MapSubmittedActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermissionRequestCode = 1001
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private var Polygon_lat_lng = ArrayList<String>()
    private val latLngslist = java.util.ArrayList<LatLng>()

    var handler: Handler = Handler()
    var runnable: Runnable? = null

    private var LAT = ArrayList<Double>()
    private var LNG = ArrayList<Double>()
    private var area: String = ""
    private var farmer_name: String = ""
    private var area_in_acers: String = ""
    private var unique_id: String = ""
    private var sub_plot_no: String = ""
    private var farmer_id: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var state: String = ""
    private var district: String = ""
    private var taluka: String = ""
    private var village: String = ""
    private var khasara_no: String = ""
    private var acers_units: String = ""
    private var imageLat: String = ""
    private var imageLng: String = ""

    private var status: Int = 0
    private var polygon_status: Int = 0

    lateinit var polygon: Polygon

    lateinit var save: ImageView
    lateinit var back: ImageView
    lateinit var txtArea: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pipe_map)

        val bundle = intent.extras
        if (bundle != null) {

            unique_id = bundle.getString("unique_id")!!
            sub_plot_no = bundle.getString("sub_plot_no")!!
            farmer_id = bundle.getString("farmer_id")!!
            latitude = bundle.getString("latitude")!!
            longitude = bundle.getString("longitude")!!
            state = bundle.getString("state")!!
            district = bundle.getString("district")!!
            taluka = bundle.getString("taluka")!!
            village = bundle.getString("village")!!
            khasara_no = bundle.getString("khasara_no")!!
            acers_units = bundle.getString("acers_units")!!
            area_in_acers  = bundle.getString("area_in_acers")!!
            area  = bundle.getString("area")!!
            farmer_name = bundle.getString("farmer_name")!!

            Polygon_lat_lng = bundle.getStringArrayList("polygon_lat_lng")!!
            status = bundle.getInt("status")
            polygon_status = bundle.getInt("polygon_status")
        } else {
            Log.e("area", "Nope")
        }

        save = findViewById(R.id.pipe_polygon_SaveLocation)
        back = findViewById(R.id.pipe_map_back)
        txtArea = findViewById(R.id.pipe_polygon_area)

        txtArea.text = "$area   $acers_units"


        back.setOnClickListener {
            mMap.clear()
            runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
            super.onBackPressed()
            finish()
        }

        save.setOnClickListener {
            var isInside = isLatLngInsidePolygon(latLngslist)
            Log.e("pipe_map_location",isInside.toString())
            if(isInside){
                if (status != 0) {
                    val intent = Intent(this, LandInfoSubmittedPreviewActivity::class.java).apply {
                        putExtra("farmer_id", farmer_id)
                        putExtra("unique_id", unique_id)
                        putExtra("sub_plot_no", sub_plot_no)
                        putExtra("latitude", latitude)
                        putExtra("longitude", longitude)
                        putExtra("state", state)
                        putExtra("district", district)
                        putExtra("taluka", taluka)
                        putExtra("village", village)
                        putExtra("khasara_no", khasara_no)
                        putExtra("acers_units", acers_units)
                        putExtra("area", area_in_acers)
                        putExtra("farmer_name", farmer_name)
                        putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(this, LandInfoPreviewActivity::class.java).apply {
                        putStringArrayListExtra("locationList", Polygon_lat_lng)
                        putExtra("farmer_id", farmer_id)
                        putExtra("unique_id", unique_id)
                        putExtra("sub_plot_no", sub_plot_no)
                        putExtra("area", area_in_acers)
                        putExtra("farmer_name", farmer_name)
                        putExtra("area", area)
                    }
                    startActivity(intent)
                }
            }
            else{
                val WarningDialog = SweetAlertDialog(this@MapSubmittedActivity, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Current location not inside \n the polygon"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        }


        mapFragment = supportFragmentManager.findFragmentById(R.id.pipe_googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@MapSubmittedActivity)
        getCurrentLocation()
    }

    private fun isLatLngInsidePolygon(latLngslist: java.util.ArrayList<LatLng>): Boolean {
        val lat = imageLat.toDouble()
        val lng = imageLng.toDouble()

        val latlng = LatLng(lat, lng)
        Log.e("pipe_map_location", latlng.toString())
        Log.e("pipe_map_location", latLngslist.toString())
        return PolyUtil.containsLocation(latlng, latLngslist, false)
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync {
                    val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

//                    imageLat = latLng.latitude.toString()
//                    imageLng = latLng.longitude.toString()
//                    Log.e("getCurrentLocation", "$imageLat-$imageLng")
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }


        for (i in 0 until Polygon_lat_lng.size){
            val dfgdg: String =  Polygon_lat_lng[i].replace("[^0-9,.-]".toRegex(), "")

            val lat: Double = dfgdg.split(",").first().toDouble()
            val lng: Double = dfgdg.split(",").last().toDouble()
            latLngslist.add(LatLng(lat, lng))

            LAT.add(lat)
            LNG.add(lng)
        }


        var polygonOptions = PolygonOptions()
        for (i in latLngslist.indices) if (i == 0) {
            polygonOptions = PolygonOptions().add(latLngslist[0])
        } else {
            mMap.clear()
            polygonOptions.add(latLngslist[i])
            Log.d("polygon123", polygonOptions.toString())
            polygonOptions.strokeColor(Color.BLACK)
            polygonOptions.strokeWidth(5f)
            polygonOptions.fillColor(0x33FF0000)
            polygon = mMap.addPolygon(polygonOptions)
        }

        for (i in 0 until LAT.size){
            mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(LAT[i], LNG[i])))
        }

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.setMinZoomPreference(15f)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }


    override fun onStart() {
        super.onStart()
        checkLocationPermission()
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(1000)
            .build()


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    private  val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            p0 ?: return

            for (location in p0.locations) {
// Handle the retrieved location
                imageLat = location.latitude.toString()
                imageLng = location.longitude.toString()
// Do something with the latitude and longitude
            }
        }
    }



    override fun onLocationChanged(location: Location) {
        val df = DecimalFormat("#.#####")
        imageLat = df.format(location.latitude).toString()
        imageLng = df.format(location.longitude).toString()
    }
}