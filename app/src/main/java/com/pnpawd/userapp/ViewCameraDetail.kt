package com.pnpawd.userapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class ViewCameraDetail : AppCompatActivity(), OnMapReadyCallback {


    lateinit var capture: ImageButton
    lateinit var toggleFlash: ImageButton
    lateinit var flipCamera: ImageButton
    private var previewView: PreviewView? = null
    var cameraFacing = CameraSelector.LENS_FACING_BACK
    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 10001
    var locationManager: LocationManager? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var addressdetails: String? = null
    var formattedDate: kotlin.String? = null
    var latitude: Double? = null
    var longitude: kotlin.Double? = null
    var showYourLocation: TextView? = null
    var showYourLocationlat: TextView? = null
    var showYourLocationlong: TextView? = null
    var showYourLocationdatetime: TextView? = null
    var currentLocation: Location? = null

    var selectimagedata : String = ""


    lateinit var sessionManager: SessionManager

    private val activityResultLauncher = registerForActivityResult<String, Boolean>(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            startCamera(cameraFacing)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_camera_detail)

        //        supportActionBar!!.hide()
        //  supportActionBar!!.isHideOnContentScrollEnabled

        previewView = findViewById(R.id.cameraPreview)
        capture = findViewById(R.id.capture)
        toggleFlash = findViewById(R.id.toggleFlash)
        flipCamera = findViewById(R.id.flipCamera)
        showYourLocation = findViewById(R.id.showYourLocation)
        showYourLocationdatetime = findViewById(R.id.showYourLocationdatetime)
        showYourLocationlong = findViewById(R.id.showYourLocationlong)
        showYourLocationlat = findViewById(R.id.showYourLocationlat)

        sessionManager = SessionManager(this@ViewCameraDetail)

        val bundle = intent.extras
        if (bundle != null) {
            selectimagedata = bundle.getString("clickimage").toString()
        }

        if (ContextCompat.checkSelfPermission(
                this@ViewCameraDetail,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activityResultLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera(cameraFacing)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        //enableUserLocation()

        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Write Function To enable gps
            locationPermission()
        } else {
            //GPS is already On then
            getLocation()
        }

        flipCamera.setOnClickListener(View.OnClickListener {
            cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera(cameraFacing)
        })
    }

    fun startCamera(cameraFacing: Int) {
        val aspectRatio = aspectRatio(previewView!!.width, previewView!!.height)
        val listenableFuture = ProcessCameraProvider.getInstance(this)
        listenableFuture.addListener({
            try {
                val cameraProvider =
                    listenableFuture.get() as ProcessCameraProvider
                val preview =
                    Preview.Builder().setTargetAspectRatio(aspectRatio).build()
                val imageCapture =
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(windowManager.defaultDisplay.rotation).build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraFacing).build()
                cameraProvider.unbindAll()
                val camera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                capture.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(
                            this@ViewCameraDetail,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    takePicture(imageCapture)
                }
                toggleFlash.setOnClickListener { setFlashIcon(camera) }
                preview.setSurfaceProvider(previewView!!.surfaceProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun takePicture(imageCapture: ImageCapture) {
        val file = File(getExternalFilesDir(null), System.currentTimeMillis().toString() + ".jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            outputFileOptions,
            Executors.newCachedThreadPool(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ViewCameraDetail,
                            "Image saved at: " + file.path,
                            Toast.LENGTH_SHORT
                        ).show()

                        //finish();
                        //System.exit(1);

                        logout_Condition(file)
                    }

                    startCamera(cameraFacing)
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ViewCameraDetail,
                            "Failed to save: " + exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    startCamera(cameraFacing)
                }
            })
    }

    private fun setFlashIcon(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                toggleFlash.setImageResource(R.drawable.baseline_flash_off_24)
            } else {
                camera.cameraControl.enableTorch(false)
                toggleFlash.setImageResource(R.drawable.baseline_flash_on_24)
            }
        } else {
            runOnUiThread {
                Toast.makeText(
                    this@ViewCameraDetail,
                    "Flash is not available currently",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = Math.max(width, height).toDouble() / Math.min(width, height)
        return if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            AspectRatio.RATIO_4_3
        } else AspectRatio.RATIO_16_9
    }

    fun locationPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Enable Your GPS Location").setCancelable(false).setPositiveButton(
            "YES"
        ) { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "NO"
            ) { dialog, which -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // reuqest for permission
            //enableUserLocation()
        } else {
            fusedLocationProviderClient!!.lastLocation.addOnCompleteListener { task ->
                //initialize location
                val location = task.result
                if (location != null) {
                    try {
                        currentLocation = location
                        //initialize geocoder
                        val geocoder = Geocoder(this@ViewCameraDetail, Locale.getDefault())

                        //initialize AddressList
                        val addresses =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        //set Latitude On Text View
                        latitude = addresses!![0].latitude

                        //set Longitude On Text View
                        longitude = addresses[0].longitude
                        addressdetails = addresses[0].getAddressLine(0)

                        //set address On Text View
                        showYourLocation!!.text = addresses[0].getAddressLine(0)
                        showYourLocationlat!!.text = latitude.toString()
                        showYourLocationlong!!.text = longitude.toString()
                        //text_addressName.setText(addresses.get(0).getAdminArea());

                        //addLocationDetails(userid,addresses.get(0).getAddressLine(0));
                        val c = Calendar.getInstance()
                        println("Current time => " + c.time)
                        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        formattedDate = df.format(c.time)
                        // formattedDate have current date/time
                        //Toast.makeText(MainActivity.this, formattedDate, Toast.LENGTH_SHORT).show();
                        showYourLocationdatetime!!.text = formattedDate
                        val supportMapFragment =
                            (supportFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment?)!!
                        supportMapFragment.getMapAsync(this@ViewCameraDetail)
                        val sharedPreferences =
                            getSharedPreferences("MySharedPref", MODE_PRIVATE)
                        val myEdit = sharedPreferences.edit()
                        myEdit.putString("address", addresses[0].getAddressLine(0))
                        myEdit.apply()
                        myEdit.commit()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("I am here!")
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        googleMap.addMarker(markerOptions)
    }

    fun logout_Condition(file: File) {

        //Show Your Another AlertDialog
        val dialog = Dialog(this@ViewCameraDetail)

        dialog.setContentView(R.layout.condition_logout)
        dialog.setCancelable(false)
        val btn_Yes = dialog.findViewById<Button>(R.id.yes)
        val finish = dialog.findViewById<Button>(R.id.finish)
        val showdatainimage = dialog.findViewById<ImageView>(R.id.showdatainimage)
        val showYourLocation = dialog.findViewById<TextView>(R.id.showYourLocation)
        val showYourLocationlat = dialog.findViewById<TextView>(R.id.showYourLocationlat)
        val showYourLocationlong = dialog.findViewById<TextView>(R.id.showYourLocationlong)
        val showYourLocationdatetime = dialog.findViewById<TextView>(R.id.showYourLocationdatetime)
        val imgBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val frameLayout = dialog.findViewById<FrameLayout>(R.id.framdata)
        // on below line we are setting bitmap to our image view.
        showdatainimage.setImageBitmap(imgBitmap)
        showYourLocation.text = addressdetails
        showYourLocationlat.text = latitude.toString()
        showYourLocationlong.text = longitude.toString()
        showYourLocationdatetime.text = formattedDate
        btn_Yes.setOnClickListener {
            //finish();
            //System.exit(1);

            btn_Yes.setVisibility(View.GONE)
            finish.setVisibility(View.GONE)

            val b: Bitmap = ScreenshotUtils.getScreenShot(frameLayout)

            if (b != null) {
                //showScreenShotImage(b);//show bitmap over imageview
                val saveFile: File = ScreenshotUtils.getMainDirectoryName(this@ViewCameraDetail) //get the path to save screenshot
                val file: File = ScreenshotUtils.store(b, "screenshot" + ScreenshotType.CUSTOM + ".jpg", saveFile) //save the screenshot to selected path


                if (selectimagedata.equals("framerimage")){
                    sessionManager.setfRAMERiMAGE(file.path)
                }else if (selectimagedata.equals("aadharimage")){
                    sessionManager.setAADHARIMAGE(file.path)
                }else if (selectimagedata.equals("otherimage")){
                    sessionManager.setOTHERIMAGE(file.path)
                }else if (selectimagedata.equals("areationimage1")){
                    sessionManager.setAERATIONIMAGE1(file.path)
                    Log.d("userdetailsdataimage",file.path)
                }else if (selectimagedata.equals("areationimage2")){
                    sessionManager.setAERATIONIMAGE2(file.path)
                }else if (selectimagedata.equals("ploatimage")){
                    sessionManager.setPLOATIMAGE(file.path)
                }else if (selectimagedata.equals("existingploatimage")){
                    sessionManager.setEXISTINGPLOATIMAGE(file.path)
                }else if (selectimagedata.equals("landinfoimage")){
                    sessionManager.setLANDINFOIMAGE(file.path)
                }else if (selectimagedata.equals("framerbenefit1")){
                    sessionManager.setFARMERBENEFIT1(file.path)
                }else if (selectimagedata.equals("framerbenefit2")){
                    sessionManager.setFARMERBENEFIT2(file.path)
                }

            } else {
                //If bitmap is null show toast message
                Toast.makeText(
                    this@ViewCameraDetail,
                    "screenshot_take_failed",
                    Toast.LENGTH_SHORT
                ).show()
            }

            dialog.dismiss()

            finish()
        }
        finish.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        //window.setBackgroundDrawableResource(R.drawable.homecard_back1);
    }
}