package com.pnpawd.userapp.polygon

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.CheckPolygonModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.maps.model.Marker
import com.pnpawd.userapp.BuildConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlin.collections.ArrayList


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mMap: GoogleMap

    var handler: Handler = Handler()
    var runnable: Runnable? = null

    var polygon_area: Double = 0.0
    var currentLat: Double = 0.0
    var currentLng: Double = 0.0
    var firstLat: Double = 0.0
    var firstLng: Double = 0.0
    private var KO: String = ""
    private var area: String = ""
    private var unique_id: String = ""
    private var farmer_id: String = ""
    private var sub_plot_no: String = ""
    private var farmer_plot_uniqueid: String = ""
    private var editable: Boolean = false

    private var polygon_date_time : String = ""
    var farmer_name: String = ""
    var threshold: String = ""

    var latLngArrayListPolygon = ArrayList<LatLng>()
    var nearbyPolygonList = ArrayList<LatLng>()
    var one = ArrayList<LatLng>()
    var two = ArrayList<LatLng>()
    val timer = Timer()
    var insideNearbyPolygonList = ArrayList<ArrayList<LatLng>>()
    var distancesFromMidPointsOfPolygonEdges = ArrayList<Double>()
    private val markerList: ArrayList<Marker> = ArrayList()
    private var Polygon_lat_lng = ArrayList<String>()
    var polygonOptions: PolygonOptions? = null
    var polygon: Polygon? = null

    var token: String = ""

    private lateinit var  delete: ImageView
    private lateinit var back: ImageView
    private lateinit var undo: ImageView
    private lateinit var save: ImageView
    private lateinit var edit: ImageView
    private lateinit var txtPolygon: TextView
    private lateinit var txtAccuracy: TextView
    private lateinit var txtAreaAcres: TextView

    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        progress = SweetAlertDialog(this@MapActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            area = bundle.getString("area")!!
            unique_id = bundle.getString("unique_id")!!
            sub_plot_no = bundle.getString("sub_plot_no")!!
            farmer_id = bundle.getString("farmer_id")!!
            farmer_plot_uniqueid = bundle.getString("farmer_plot_uniqueid")!!
            threshold = bundle.getString("threshold")!!
            farmer_name = bundle.getString("farmer_name")!!
        } else {
            Log.e("area", "Nope")
        }

        // Check if Developer Option is on
        if (BuildConfig.DEBUG) {
            Log.e("CHECKKK", "in if Debug")
        } else {
            Log.e("CHECKKK", "in else Release")
            warnAboutDevOpt()
        }



        save = findViewById(R.id.ivSaveLocation)
        delete = findViewById(R.id.bin)
        back = findViewById(R.id.ivBackMap)
        undo = findViewById(R.id.backArrow)
        edit = findViewById(R.id.edit)
        txtPolygon = findViewById(R.id.polygon_area)
        txtAccuracy = findViewById(R.id.polygon_accuracy)
        txtAreaAcres = findViewById(R.id.area_acres)

        delete.setOnClickListener {
            if (polygon != null)
                polygon!!.remove()
            latLngArrayListPolygon.clear()
            two.clear()

            try {
                Polygon_lat_lng.clear()
                Polygon_lat_lng.removeAll(Polygon_lat_lng)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            txtPolygon.text = ""
            editable = false
            mMap.clear()

            getRadiusPolygon(firstLat, firstLng)
        }

        edit.setOnClickListener {
//Creating the instance of PopupMenu
            val popup = PopupMenu(this@MapActivity, edit)

//Inflating the Popup using xml file
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

//registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.multipleMarker -> {
                        editable = true
                        true
                    }

                    else -> false
                }
            }
//showing popup menu
            popup.show()
        }

        save.setOnClickListener {
            if (Polygon_lat_lng.size < 3) {
                Toast.makeText(this@MapActivity, "Markers less than 3", Toast.LENGTH_SHORT).show()
            } else {
// Calculating meters from polygon list
                val m = SphericalUtil.computeArea(latLngArrayListPolygon)
                Log.e("m", "computeArea $m")

// converting meters to acers
                val df = DecimalFormat("#.#####")
                val a = df.format(m * 0.000247105).toDouble()
                Log.e("a", "computeArea $a")

                val per = threshold.toDouble()
                val dif = area.toDouble() * per
                Log.e("dif", dif.toString())

                val minus = area.toDouble() - dif
                Log.e("minus", minus.toString())

                val add = area.toDouble() + dif
                Log.e("add", add.toString())

                if (polygon_area > minus && polygon_area < add) {
                    runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible

                    val stringList = convertLatLngListToStringList(latLngArrayListPolygon)
                    val intent = Intent(this, LandInfoActivity::class.java).apply {
                        putExtra("locationList", stringList)
                        putExtra("area", area)
                        putExtra("unique_id", unique_id)
                        putExtra("sub_plot_no", sub_plot_no)
                        putExtra("farmer_id", farmer_id)
                        putExtra("polygon_area", polygon_area)
                        putExtra("farmer_plot_uniqueid", farmer_plot_uniqueid)
                        putExtra("polygon_date_time", polygon_date_time)
                        putExtra("farmer_name", farmer_name)
                    }
                    startActivity(intent)
                    finish()
                } else if (polygon_area < minus) {
                    val WarningDialog =
                        SweetAlertDialog(this@MapActivity, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Area drawn is less than plot \n area"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                } else if (polygon_area > add) {
                    val WarningDialog =
                        SweetAlertDialog(this@MapActivity, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Area drawn is more than plot \n area"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                }
            }
        }

        back.setOnClickListener {
            mMap.clear()
            runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
            super.onBackPressed()
            finish()
        }

//Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@MapActivity)
        gpsCheck()
        showAcres()
    }

    private fun warnAboutDevOpt() {
        val adb = Settings.Secure.getInt(this.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        if(adb == 1) {
            android.app.AlertDialog.Builder(this@MapActivity)
                .setTitle("Developer Option Detected")
                .setMessage("Developer options are enabled on phone. Disable to continue using the app.")
                .setCancelable(false)
                .setNegativeButton(
                    R.string.close_app,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        finishAffinity()
                        finish()
                    })
                .setPositiveButton(R.string.clcik_disable,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        this@MapActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    })
                .show()
        }
    }

    private fun gpsCheck() {
        val active = isLocationEnabled(this@MapActivity)
        Log.e("active", active.toString())

        if (!active) {
            android.app.AlertDialog.Builder(this@MapActivity)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.yes) { paramDialogInterface, paramInt ->
                    this@MapActivity.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
                .setNegativeButton(
                    R.string.no
                ) { paramDialogInterface, paramInt ->
                    gpsCheck()
                }
                .show()
        }
        else{
            checkLocationPermission()
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        else{
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null) {
                    mapFragment.getMapAsync(OnMapReadyCallback {
                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                        firstLat = latLng.latitude
                        firstLng = latLng.longitude

                        Log.e("getCurrentLocation", latLng.latitude.toString() + "-" + latLng.longitude)
                        getRadiusPolygon(firstLat, firstLng)
                    })
                }

            }
        }
    }

    private fun getRadiusPolygon(firstLat: Double, firstLng: Double) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.checking_data)
        progress.setCancelable(false)
        progress.show()

        one.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.polygonNearby("Bearer $token", farmer_plot_uniqueid, firstLat, firstLng).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null) {
                        progress.dismiss()

                        val jsonArray = JSONArray(response.body()!!.string())

                        for (i in 0 until jsonArray.length()){

                            var innerArray = jsonArray.getJSONArray(i)
                            for (k in 0 until innerArray.length()){

                                val innerObject = innerArray.getJSONObject(k)
                                val rangeArray = innerObject.getJSONArray("ranges")
                                for (j in 0 until rangeArray.length()){
                                    val jsonObject1 = rangeArray.getJSONObject(j)

                                    val latitude = jsonObject1.optString("lat").toDouble()
                                    val longitude = jsonObject1.optString("lng").toDouble()

                                    val latLng = LatLng(latitude, longitude)
                                    nearbyPolygonList.add(latLng)
                                    one.add(latLng)
                                }

                                Log.e("NearbyPolygonList", nearbyPolygonList.toString())
                                insideNearbyPolygonList.add(nearbyPolygonList)
                                Log.e("insideNearbyPolygonList", insideNearbyPolygonList.toString())

                                val polygonOptions = PolygonOptions()

                                for (j in 0 until nearbyPolygonList.size){
                                    val latitude = nearbyPolygonList[j].latitude
                                    val longitude = nearbyPolygonList[j].longitude

                                    polygonOptions.add(LatLng(latitude,longitude))
                                    polygonOptions.strokeColor(Color.CYAN)
                                    polygonOptions.strokeWidth(4f)
                                    polygonOptions.fillColor(Color.CYAN)
                                    val polygon: Polygon = mMap.addPolygon(polygonOptions)
                                }
                                nearbyPolygonList.clear()
                            }

                        }
                    }
                    else if(response.body() == null){
                        progress.dismiss()
                    }
                }
                else{
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@MapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun plotMultiplePolygons() {
        if (insideNearbyPolygonList.isEmpty()){
            Log.e("polygonListEmpty", "Polygon List is Empty")
        }
        else{
            val latLng = insideNearbyPolygonList[0]
            drawPolygons(latLng)
        }
    }

    private fun drawPolygons(latLng: ArrayList<LatLng>) {
        for (j in 0 until latLng.size){
            val latitude = latLng[j].latitude
            val longitude = latLng[j].longitude
            mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(latitude, longitude)).title(j.toString()))
            Log.e("marker_added", "Marker Added")
        }

        val polygonOptions = PolygonOptions()

        polygonOptions.addAll(latLng)
        polygonOptions.strokeColor(Color.RED)
        polygonOptions.strokeWidth(4f)
        polygonOptions.fillColor(0x33FF0000)
        polygon = mMap.addPolygon(polygonOptions)

        insideNearbyPolygonList.removeAt(0)
        plotMultiplePolygons()
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("else", "if_Small")
                AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("The app needs to access current location, please accept to use location functionality")
                    .setPositiveButton(
                        "OK") { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                Log.e("else", "else_small")
                AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("The app needs to access current location, please accept to use location functionality")
                    .setPositiveButton(
                        "OK") { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            }
        } else {
            Log.e("else", "else_big")
            getCurrentLocation()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
    }

    @SuppressLint("PotentialBehaviorOverride", "SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        getLocationAccuracy()

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        undo.setOnClickListener {
            try {
                if (!markerList.isEmpty()) {
                    Log.e("latLngArrayListPolygon", latLngArrayListPolygon.size.toString())
                    Log.e("Polygon_lat_lng", Polygon_lat_lng.size.toString())

                    val lastMarker: Marker = markerList.removeAt(markerList.size - 1)
                    lastMarker.remove()
                    Polygon_lat_lng.removeAt(Polygon_lat_lng.size - 1)
                    latLngArrayListPolygon.removeAt(latLngArrayListPolygon.size - 1)
                    two.removeAt(two.size - 1)
                    polygon?.remove()

                    val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker, null)
                    for (i in latLngArrayListPolygon.indices)
                        if (i == 0) {
                            polygonOptions = PolygonOptions().add(latLngArrayListPolygon[0])
                            mMap.clear()
                        } else {
                            Log.e("Polygon_lat_lng", "else")
                            polygonOptions!!.add(latLngArrayListPolygon[i])
                            mMap.clear()
                        }

                    for (i in latLngArrayListPolygon.indices) {
                        mCurrLocationMarker = mMap.addMarker(
                            MarkerOptions().anchor(0.5f, 0.5f).position(latLngArrayListPolygon[i])
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        pickupMarkerDrawable.toBitmap(
                                            pickupMarkerDrawable.intrinsicWidth,
                                            pickupMarkerDrawable.intrinsicHeight,
                                            null
                                        )
                                    )
                                )
                        )
                    }

                    polygonOptions!!.strokeColor(Color.RED)
                    polygonOptions!!.strokeWidth(4f)
                    polygonOptions!!.fillColor(Color.RED)
                    polygon = mMap.addPolygon(polygonOptions!!)

                    txtPolygon.text = ""

// Calculating meters from polygon list
                    val m = SphericalUtil.computeArea(latLngArrayListPolygon)
                    Log.e("m", "computeArea $m")

// converting meters to acers
                    val df = DecimalFormat("#.#####")
                    polygon_area = df.format(m * 0.000247105).toDouble()
                    Log.e("a", "computeArea $polygon_area")

                    txtPolygon.text = polygon_area.toString() + "  acres"

                    if (Polygon_lat_lng.size == 0) {
                        mMap.clear()
                    }

                    getRadiusPolygon(firstLat, firstLng)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mMap.setOnMapClickListener {  latLng ->
            if (editable) {

                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_load)
                progress.setCancelable(false)
                progress.show()

                checkCoordinates(latLng)
            }
        }


        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            override fun onMarkerDragStart(marker: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                pointsOverlap(marker.title, marker)
            }

            override fun onMarkerDrag(marker: Marker) {}
        })

        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    private fun pointsOverlap(title: String?, marker: Marker) {
        val latLng = LatLng(marker.position.latitude, marker.position.longitude)
        Log.e("latitude_longitude", latLng.toString())

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val checkPolygonModel = CheckPolygonModel(farmer_plot_uniqueid, latLng.latitude, latLng.longitude)

        apiInterface.checkCoordinates("Bearer $token", checkPolygonModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    Toast.makeText(this@MapActivity, "Point overlapping", Toast.LENGTH_SHORT).show()
                    pointOverlappingMsg()
                    resetMarkers()
                }
                else if (response.code() == 422){
                    updateMarkerOnDrag(title!!, marker, true)
                    getRadiusPolygon(firstLat, firstLng)
                }
                else{
                    resetMarkers()
                    getRadiusPolygon(firstLat, firstLng)
                    Log.e("CheckCoordinates", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
                resetMarkers()
            }
        })
    }

    private fun updateMarkerOnDrag(latLng: String, marker: Marker, ready: Boolean) {
        val pos: Int = latLng.toInt()

        Log.e("polygon_size", latLngArrayListPolygon.size.toString())
        Log.e("polygon_size_pos", pos.toString())

        latLngArrayListPolygon.removeAt(pos)
        latLngArrayListPolygon.add(pos, LatLng(marker.position.latitude, marker.position.longitude))


        Log.e("dragged", latLngArrayListPolygon.toString())
        if (ready){
            mMap.clear()
            plotPolygons(latLngArrayListPolygon)
        }
    }

    private fun resetMarkers() {
        mMap.clear()

        val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker,null)
        var mark: Marker?

        for (j in 0 until latLngArrayListPolygon.size){
            val latitude = latLngArrayListPolygon[j].latitude
            val longitude = latLngArrayListPolygon[j].longitude
            mark = mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(latitude, longitude)).icon(BitmapDescriptorFactory
                .fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth, pickupMarkerDrawable.intrinsicHeight, null)))
                .draggable(true).title(j.toString()))
            mark?.tag = j.toString()
        }

        val polygonOptions = PolygonOptions()
        polygonOptions.addAll(latLngArrayListPolygon)
        polygonOptions.strokeColor(Color.RED)
        polygonOptions.strokeWidth(4f)
        polygonOptions.fillColor(Color.RED)
        polygon = mMap.addPolygon(polygonOptions)

        calculateDistance(latLngArrayListPolygon)
        getRadiusPolygon(firstLat, firstLng)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun plotPolygons(latLng: ArrayList<LatLng>) {
        val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker,null)
        var mark: Marker?

        for (j in 0 until latLng.size){
            val latitude = latLng[j].latitude
            val longitude = latLng[j].longitude
            mark = mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth, pickupMarkerDrawable.intrinsicHeight,
                    null))).draggable(true).title(j.toString()))
            mark?.tag = j.toString()
        }

        val polygonOptions = PolygonOptions()
        polygonOptions.addAll(latLng)
        polygonOptions.strokeColor(Color.RED)
        polygonOptions.fillColor(Color.RED)
        polygon = mMap.addPolygon(polygonOptions)

        calculateDistance(latLngArrayListPolygon)
    }

    private fun checkCoordinates(latLng: LatLng){
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val checkPolygonModel = CheckPolygonModel(farmer_plot_uniqueid, latLng.latitude, latLng.longitude)
        apiInterface.checkCoordinates("Bearer $token", checkPolygonModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null){
                        val jsonObject = JSONObject(response.body()!!.string())
                        val status  =  jsonObject.optBoolean("status")

                        progress.dismiss()

                        pointOverlappingMsg()
                        Toast.makeText(this@MapActivity, "Point overlapping", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        progress.dismiss()
                    }
                }
                else if (response.code() == 422){
                    progress.dismiss()
                    addMarker(latLng)
                }
                else{
                    progress.dismiss()
                    Log.e("CheckCoordinaters", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()

            }
        })
    }

    private fun pointOverlappingMsg() {
        val WarningDialog = SweetAlertDialog(this@MapActivity, SweetAlertDialog.WARNING_TYPE)

        WarningDialog.titleText = resources.getString(R.string.warning)
        WarningDialog.contentText = resources.getString(R.string.point_overlapping_warning)
        WarningDialog.confirmText = resources.getString(R.string.ok)
        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun addMarker(latLng: LatLng) {
        val LATLNG = LatLng(firstLat, firstLng)
        val distance = SphericalUtil.computeDistanceBetween(latLng, LATLNG)

        if (latLngArrayListPolygon.size == 0 && distance > 20) {
            Toast.makeText(this@MapActivity, "Distance is greater than 10 meters", Toast.LENGTH_SHORT).show()
        } else {
            var count = 0
            two.add(latLng)

            for (j in 0 until one.size) {
                val validLocation = PolyUtil.containsLocation(one[j], two, false)
                Log.e("Distance_greater", validLocation.toString())
                if (validLocation) {
                    Log.e("Distance_greater", validLocation.toString())
                    Toast.makeText(this@MapActivity, "Point overlapping", Toast.LENGTH_SHORT).show()
                    two.removeAt(two.size - 1)
                    count = 1

                    val WarningDialog = SweetAlertDialog(this@MapActivity, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.point_overlapping_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                    break
                }
            }

            if (count == 0) {
                val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker,null)
                mCurrLocationMarker = mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth,
                        pickupMarkerDrawable.intrinsicHeight, null))))
                mCurrLocationMarker?.tag = latLng

                val c: Calendar = Calendar.getInstance()
                val dfi = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val formattedDate: String = dfi.format(c.time)
                polygon_date_time = formattedDate


                adjustPolygonWithRespectTo(latLng)

                currentLat = latLng.latitude
                currentLng = latLng.longitude


                for (i in latLngArrayListPolygon.indices) if (i == 0) {
                    val titleNumber = latLngArrayListPolygon.size - 1
                    val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker,null)
                    mCurrLocationMarker = mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth,
                                pickupMarkerDrawable.intrinsicHeight, null))).draggable(true).title(titleNumber.toString()))
                    polygonOptions = PolygonOptions().add(latLngArrayListPolygon[0])

                } else {
                    polygonOptions!!.add(latLngArrayListPolygon[i])
                    polygonOptions!!.strokeColor(Color.RED)
//                    polygonOptions!!.strokeWidth(5f)
                    polygonOptions!!.fillColor(Color.RED)
                    polygon = mMap.addPolygon(polygonOptions!!)

                    mCurrLocationMarker?.let { markerList.add(it) }
                    calculateDistance(latLngArrayListPolygon)
                }

                for (i in latLngArrayListPolygon.indices) {
                    val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker,null)
                    mCurrLocationMarker = mMap.addMarker(
                        MarkerOptions().anchor(0.5f, 0.5f).position(latLngArrayListPolygon[i])
                            .icon(BitmapDescriptorFactory.fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth,
                                pickupMarkerDrawable.intrinsicHeight, null))))
                }

// Getting the marker Lat & Lng and storing it in variable. It is accessible from "latLng".
                KO = latLng.toString()

// Replacing or trimming all the text that are unnecessary and only keeping the , . & numbers.
                KO = KO.replace("[^0-9,.]".toRegex(), "").trim { it <= ' ' }
                Polygon_lat_lng.add(KO)
                Log.e("Polygon", Polygon_lat_lng.toString())

//                txtPolygon.text = ""
            }
            else{
                count = 0
            }
        }
    }

    private fun calculateDistance(latLngArrayListPolygon: java.util.ArrayList<LatLng>) {
// Calculating meters from polygon list
        val m = SphericalUtil.computeArea(latLngArrayListPolygon)
        Log.e("m", "computeArea $m")

// converting meters to acers
        val df = DecimalFormat("#.#####")
        polygon_area = df.format(m * 0.000247105).toDouble()
        Log.e("a", "computeArea $polygon_area")
        txtPolygon.text = "$polygon_area  acres"
    }

    fun convertLatLngListToStringList(latlngList: ArrayList<LatLng>): ArrayList<String> {
        val stringList = ArrayList<String>()

        // Map each LatLng object to its string representation
        for (latlng in latlngList) {
            val lat = latlng.latitude
            val lng = latlng.longitude
            val latlngString = "$lat,$lng"
            stringList.add(latlngString)
        }

        return stringList
    }



    private fun adjustPolygonWithRespectTo(point: LatLng) {
        var minDistance = 0.0
        if (latLngArrayListPolygon.size > 2) {
            distancesFromMidPointsOfPolygonEdges.clear()
            for (i in latLngArrayListPolygon.indices) {

// 1. Find the mid points of the edges of polygon.
                val list = ArrayList<LatLng>()
                if (i == latLngArrayListPolygon.size - 1) {
                    list.add(latLngArrayListPolygon[latLngArrayListPolygon.size - 1])
                    list.add(latLngArrayListPolygon[0])
                } else {
                    list.add(latLngArrayListPolygon[i])
                    list.add(latLngArrayListPolygon[i + 1])
                }
                val midPoint: LatLng = computeCentroid(list)

// 2. Calculate the nearest coordinate by finding distance between mid point of each edge and the coordinate to be drawn.
                val startPoint = Location("")
                startPoint.latitude = point.latitude
                startPoint.longitude = point.longitude
                val endPoint = Location("")
                endPoint.latitude = midPoint.latitude
                endPoint.longitude = midPoint.longitude
                val distance = startPoint.distanceTo(endPoint).toDouble()
                distancesFromMidPointsOfPolygonEdges.add(distance)
                if (i == 0) {
                    minDistance = distance
                } else {
                    if (distance < minDistance) {
                        minDistance = distance
                    }
                }
            }
        }

// 5. Now add coordinated to be drawn
        latLngArrayListPolygon.add(point)
    }


    private fun computeCentroid(points: List<LatLng>): LatLng {
        val latitude = latLngArrayListPolygon[0].latitude
        val longitude = latLngArrayListPolygon[0].longitude
        Log.d("pokemon", (latitude + longitude).toString())
        return LatLng(latitude, longitude)
    }

    override fun onBackPressed() {
        runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
        super.onBackPressed()
        finish()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
        private fun getLocationAccuracy() {
        val startingNumber = 20
        var currentNumber = startingNumber

        lifecycleScope.launch {
            while (currentNumber >= 3) {
                println(currentNumber)
                currentNumber--

                delay(2000) // Pause for 2 seconds
                txtAccuracy.text = "Accuracy : $currentNumber meters"
            }
        }

    }


    override fun onPause() {
        runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
        super.onPause()
    }


    private fun showAcres() {
        txtAreaAcres.text = "$area  acres"
    }
}