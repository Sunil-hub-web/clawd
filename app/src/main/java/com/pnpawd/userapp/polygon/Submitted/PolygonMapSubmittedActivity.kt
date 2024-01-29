package com.pnpawd.userapp.polygon.Submitted

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import com.pnpawd.userapp.R
import com.pnpawd.userapp.pipeinstallation.LandInfoPreviewActivity
import com.pnpawd.userapp.pipeinstallation.Submitted.LandInfoSubmittedPreviewActivity
import java.text.DecimalFormat

class PolygonMapSubmittedActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
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
    lateinit var ok: Button
    lateinit var txtSubmitted: TextView


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polygon_map_submitted)

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

        ok = findViewById(R.id.polygon_submitted_ok)
        back = findViewById(R.id.polygon_submitted_back)
        txtSubmitted = findViewById(R.id.polygon_submitted)
        txtSubmitted.text = "Polygon already submitted"


        back.setOnClickListener {
            mMap.clear()
            runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
            super.onBackPressed()
            finish()
        }

        ok.setOnClickListener{
            super.onBackPressed()
            finish()
        }


        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@PolygonMapSubmittedActivity)
        getCurrentLocation()
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync(OnMapReadyCallback {
                    val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

                    imageLat = latLng.latitude.toString()
                    imageLng = latLng.longitude.toString()
                    Log.e("getCurrentLocation", "$imageLat-$imageLng")
                })
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
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        for (i in 0 until Polygon_lat_lng.size){
            val dfgdg: String =  Polygon_lat_lng[i].replace("[^0-9,.]".toRegex(), "")

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

        mMap.setMinZoomPreference(15f)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    override fun onLocationChanged(location: Location) {
        val df = DecimalFormat("#.#####")
        imageLat = df.format(location.latitude).toString()
        imageLng = df.format(location.longitude).toString()
    }
}