package com.pnpawd.userapp.reports.pipe_report

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.pnpawd.userapp.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class PipeReportImageActivity : AppCompatActivity() {
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var image1: ImageView
    private lateinit var imgBack: ImageView
    private lateinit var submit: Button

//    private var imageNumber = 0
    var rotate = 0
    private var token: String = ""
    private var imageLat: String = ""
    private var imageLng: String = ""
    private lateinit var farmer_plot_uniqueid: String
    private lateinit var pipe_no: String
    private lateinit var unique_id: String
    private lateinit var distance: String
    private lateinit var plot_no: String
    private lateinit var pipe_img_id: String
    private lateinit var progress: SweetAlertDialog

    private var imageModelPath1: String = ""
    private lateinit var pipeImageLatitude: String
    private lateinit var pipeImageLongitude: String

    val watermark: Common = Common()
    lateinit var uri: Uri
    lateinit var photoPath: File
    lateinit var currentPhotoPath: String
    var imageFileName: String = ""

    var arrayList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pipe_report_image)
        progress = SweetAlertDialog(this@PipeReportImageActivity, SweetAlertDialog.PROGRESS_TYPE)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        imgBack = findViewById(R.id.ImageRe_back)

        val bundle = intent.extras
        if (bundle != null) {
            pipe_img_id = bundle.getString("pipe_img_id").toString()
            farmer_plot_uniqueid = bundle.getString("farmer_uniqueId").toString()
            unique_id = bundle.getString("uniqueId")!!
            pipeImageLatitude = bundle.getString("lat")!!
            pipeImageLongitude = bundle.getString("lng")!!
            plot_no = bundle.getString("plot_no")!!
            pipe_no = bundle.getString("pipe_no")!!
            distance = bundle.getString("distance")!!

        } else {
            Log.e("unique_id", "Nope")
        }

        submit = findViewById(R.id.re_upload_image_submit)

        image1 = findViewById(R.id.image_re_upload_1)
        image1.setOnClickListener(View.OnClickListener {
            getActualLocation()
        })

        imgBack.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })


        submit.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@PipeReportImageActivity, SweetAlertDialog.WARNING_TYPE)

            if (imageModelPath1.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Click image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else {
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                sendData(imageModelPath1)
            }

        })

        checkGPS()
    }

    private fun sendData(imageModelPath1: String) {

        val file = File(imageModelPath1)
        val farmer_unique_id = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val plot_uniqueid =  farmer_plot_uniqueid.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitude = pipeImageLatitude.toRequestBody("text/plain".toMediaTypeOrNull())
        val longitude = pipeImageLongitude.toRequestBody("text/plain".toMediaTypeOrNull())
        val dis = distance.toRequestBody("text/plain".toMediaTypeOrNull())
        val plot = plot_no.toRequestBody("text/plain".toMediaTypeOrNull())
        val pipe = pipe_no.toRequestBody("text/plain".toMediaTypeOrNull())
        val entryId = pipe_img_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmerUniqueID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, farmer_unique_id)
        val farmerID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_plot_uniqueid", null, plot_uniqueid)
        val plotNumber: MultipartBody.Part = MultipartBody.Part.createFormData("plot_no", null, plot)
        val pipeNumber: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_no", null, pipe)
        val Latitude: MultipartBody.Part = MultipartBody.Part.createFormData("lat", null, latitude)
        val Longitude: MultipartBody.Part = MultipartBody.Part.createFormData("lng", null, longitude)
        val Distance: MultipartBody.Part = MultipartBody.Part.createFormData("distance", null, dis)
        val pipeID: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_img_id", null, entryId)

        val body: MultipartBody.Part = MultipartBody.Part.createFormData("images", file.name, requestFile)


        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.pipeReUploadImage("Bearer $token", farmerUniqueID, farmerID, pipeNumber, plotNumber, Latitude, Longitude, Distance, pipeID, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200){
                        nextScreen()
                    }
                    else{
                        progress.dismiss()
                        Log.e("response_code", response.code().toString())
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progress.dismiss()
                }
            })
    }

    private fun nextScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@PipeReportImageActivity, SweetAlertDialog.SUCCESS_TYPE)
        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Data Sent Successfully. "
        SuccessDialog.confirmText = " OK "
        SuccessDialog.showCancelButton(false)
        SuccessDialog.setCancelable(false)
        SuccessDialog.setConfirmClickListener {

            SuccessDialog.cancel()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }.show()
    }

    private fun getActualLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.location)
        progress.setCancelable(false)
        progress.show()

        mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            requestNewLocationData()
        }
    }
    private fun requestNewLocationData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        var mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(0)
            .setMaxUpdateDelayMillis(5000)
            .build()


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    @SuppressLint("NotifyDataSetChanged")
    private var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//                arrayList.add(currentPhotoPath)
                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)

// Setting the image straight if the orientation of the image is wrong while clicking the image.
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }

// Adding watermark to the image.
                val edittedImage = watermark.addWatermark(application.applicationContext, image, "#$unique_id $timeStamp | $imageLat | $imageLng")
                image1.setImageBitmap(edittedImage)
                image1.rotation = rotate.toFloat()

                try {
                    val bitmap = edittedImage

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

// Compressing the new watermarked image.
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

// Storing the path of the watermarked image.
                    imageModelPath1 = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
//            Log.e("mLastLocation.latitude", mLastLocation?.latitude.toString())
//            Log.e("mLastLocation.longitude", mLastLocation?.longitude.toString())

            imageLat = mLastLocation?.latitude.toString()
            imageLng = mLastLocation?.longitude.toString()

            val df = DecimalFormat("#####")
            val distance = df.format(SphericalUtil.computeDistanceBetween(LatLng(imageLat.toDouble(), imageLng.toDouble()), LatLng(pipeImageLatitude.toDouble(), pipeImageLongitude.toDouble())))
            Log.e("pipeImageLatitude", pipeImageLatitude)
            Log.e("pipeImageLongitude", pipeImageLongitude)

            Log.e("imageLat", imageLat)
            Log.e("imageLng", imageLng)

//            if(distance.toDouble() < 8) {
                stop()
//            }
//            else{
//                val WarningDialog = SweetAlertDialog(this@PipeReportImageActivity, SweetAlertDialog.WARNING_TYPE)
//
//                WarningDialog.titleText = resources.getString(R.string.warning)
//                WarningDialog.contentText = "Distance should be less than ${8} meters"
//                WarningDialog.confirmText = " OK "
//                WarningDialog.showCancelButton(false)
//                WarningDialog.setCancelable(false)
//                WarningDialog.setConfirmClickListener {
//                    WarningDialog.cancel()
//                }.show()
//
//                stopAgain()
//            }
        }
    }

    private fun stop() {
        Log.e("Stopped", "Location Update Stopped")
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        progress.dismiss()

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            try {
                photoPath = createImageFile()
            } catch (ex: IOException) {}
// Continue only if the File was successfully created
            if (photoPath != null) {
                uri = FileProvider.getUriForFile(
                    this@PipeReportImageActivity,
                    BuildConfig.APPLICATION_ID + ".provider", photoPath
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                resultLauncher1.launch(intent)
            }
        }
    }

    private fun stopAgain() {
        Log.e("Stopped", "Location Update Stopped")
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        progress.dismiss()
    }


    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
// Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

// Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        Log.i("imagepath", currentPhotoPath)
        return image
    }

    private fun checkGPS() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true)
        val locationSettingsResponseTask = LocationServices.getSettingsClient(applicationContext).checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    val resolvableApiException = e as ResolvableApiException
                    try {
                        resolvableApiException.startResolutionForResult(this@PipeReportImageActivity, MY_PERMISSIONS_REQUEST_LOCATION)
                    } catch (sendIntentException: IntentSender.SendIntentException) {
                        sendIntentException.printStackTrace()
                    }
                }
                if (e.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(this@PipeReportImageActivity, "setting not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}