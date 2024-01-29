package com.pnpawd.userapp.reports.aeriation_report

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.pnpawd.userapp.R
import okhttp3.MultipartBody
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AeriationReportImageActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var locationManager: LocationManager? = null
    private lateinit var farmer_plot_uniqueid: String
    private lateinit var pipe_installation_id: String
    private lateinit var pipe_no: String
    private lateinit var unique_id: String
    private lateinit var aeration_no: String
    private lateinit var plot_no: String

    private var token: String = ""
    private var imageLat: String = ""
    private var imageLng: String = ""
    val watermark: Common = Common()

    lateinit var uri: Uri
    lateinit var photoPath: File
    lateinit var currentPhotoPath: String
    var imageFileName: String = ""
    private var pipeImageLatitude = 0.0
    private var pipeImageLongitude = 0.0
    private var imageNumber = 0
    var rotate = 0

    private var imageModelPath1: String = ""
    private var imageModelPath2: String = ""

    var imagePathList = ArrayList<String>()
    var arrayList = ArrayList<String>()
    var pipeImageLocation = ArrayList<String>()

    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var ivback : ImageView

    private lateinit var btnSubmit: Button

    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_reupload)
        progress = SweetAlertDialog(this@AeriationReportImageActivity, SweetAlertDialog.PROGRESS_TYPE)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            pipe_no = bundle.getString("pipe_no")!!
            pipe_installation_id = bundle.getString("pipe_installation_id")!!
            farmer_plot_uniqueid = bundle.getString("farmer_plot_uniqueid").toString()
            unique_id = bundle.getString("unique_id")!!
            pipeImageLatitude = bundle.getDouble("latitude")
            pipeImageLongitude = bundle.getDouble("longitude")
            aeration_no = bundle.getString("aeration_no")!!
            plot_no = bundle.getString("plot_no")!!
        } else {
            Log.e("unique_id", "Nope")
        }

        ivback = findViewById(R.id.ImageRe_back)
        btnSubmit = findViewById(R.id.re_upload_image_submit)

        image1 = findViewById(R.id.image_re_upload_1)
        image2 = findViewById(R.id.image_re_upload_2)

        image1.setOnClickListener(View.OnClickListener {
            imageNumber = 1
            getActualLocation(1)
        })

        image2.setOnClickListener(View.OnClickListener {
            imageNumber = 2
            getActualLocation(2)
        })

        ivback.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })

        btnSubmit.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@AeriationReportImageActivity, SweetAlertDialog.WARNING_TYPE)

            if (imageModelPath1.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Click image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if(imageModelPath1.isNotEmpty() && imageModelPath2.isEmpty()){
                imagePathList.add(imageModelPath1)

                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                sendData(imagePathList)
            }
            else if (imageModelPath1.isNotEmpty() && imageModelPath2.isNotEmpty()) {
                imagePathList.add(imageModelPath1)
                imagePathList.add(imageModelPath2)

                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                sendData(imagePathList)
            }
        })

    }

    private fun sendData(imagePathList: ArrayList<String>) {
        val listOfImages = ArrayList<MultipartBody.Part>()
        for (i in 0 until imagePathList.size) {
            val propertyImageFile = File(imagePathList[i])
            listOfImages.add(prepareFilePart("image[$i]", propertyImageFile))
        }

        val pipeInstallationId = pipe_installation_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueId = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val farmerPlotUniqueId = farmer_plot_uniqueid.toRequestBody("text/plain".toMediaTypeOrNull())
        val plotNo = plot_no.toRequestBody("text/plain".toMediaTypeOrNull())
        val aeriationNo = aeration_no.toRequestBody("text/plain".toMediaTypeOrNull())
        val pipeNo = pipe_no.toRequestBody("text/plain".toMediaTypeOrNull())

        val pipe_installation_id: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_installation_id", null, pipeInstallationId)
        val farmer_uniqueId: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, uniqueId)
        val farmer_plot_uniqueid: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_plot_uniqueid", null, farmerPlotUniqueId)
        val plot_no: MultipartBody.Part = MultipartBody.Part.createFormData("plot_no", null, plotNo)
        val aeration_no: MultipartBody.Part = MultipartBody.Part.createFormData("aeration_no", null, aeriationNo)
        val pipe_no: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_no", null, pipeNo)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.sendAeriationReportImage("Bearer $token", pipe_installation_id, farmer_uniqueId, farmer_plot_uniqueid, plot_no, aeration_no,
            pipe_no, listOfImages).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    Toast.makeText(this@AeriationReportImageActivity, "Data sent successfully", Toast.LENGTH_SHORT).show()
                    homeScreen()

                }
                else{
                    Toast.makeText(this@AeriationReportImageActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AeriationReportImageActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    private fun homeScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@AeriationReportImageActivity, SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Data Submitted Successfully. "
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


    private fun getActualLocation(imageNumber: Int) {
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

        var mlocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(0)
            .setMaxUpdateDelayMillis(5000)
            .build()


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mlocationRequest, mLocationCallback, Looper.myLooper())
    }


    @SuppressLint("NotifyDataSetChanged")
    private var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                arrayList.add(currentPhotoPath)
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

    @SuppressLint("NotifyDataSetChanged")
    private var resultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                arrayList.add(currentPhotoPath)
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
                image2.setImageBitmap(edittedImage)
                image2.rotation = rotate.toFloat()

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
                    imageModelPath2 = outFile.absolutePath
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
            Log.e("mLastLocation.latitude", mLastLocation?.latitude.toString())
            Log.e("mLastLocation.longitude", mLastLocation?.longitude.toString())

            imageLat = mLastLocation?.latitude.toString()
            imageLng = mLastLocation?.longitude.toString()

            val df = DecimalFormat("#####")
            val distance = df.format(SphericalUtil.computeDistanceBetween(LatLng(imageLat.toDouble(), imageLng.toDouble()), LatLng(pipeImageLatitude, pipeImageLongitude)))

            if(distance.toDouble() < 15) {
                stop()
            }
            else{
                val WarningDialog = SweetAlertDialog(this@AeriationReportImageActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Distance should be less than ${15} meters"
                WarningDialog.confirmText = " OK "
                WarningDialog.showCancelButton(false)
                WarningDialog.setCancelable(false)
                WarningDialog.setConfirmClickListener {
                    WarningDialog.cancel()
                }.show()

                stopAgain()
            }
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
                    this@AeriationReportImageActivity,
                    BuildConfig.APPLICATION_ID + ".provider", photoPath
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                if (imageNumber == 1){
                    resultLauncher1.launch(intent)
                }
                else if (imageNumber == 2) {
                    resultLauncher2.launch(intent)
                }
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

}