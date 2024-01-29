package com.pnpawd.userapp.reports.farmer_report

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
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
import androidx.core.graphics.drawable.toBitmap
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.pnpawd.userapp.R
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ImageReuploadActivity : AppCompatActivity(), LocationListener {
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var locationManager: LocationManager? = null
    private var lat1: Double = 0.0
    private var lng1: Double = 0.0
    private var lat2: Double = 0.0
    private var lng2: Double = 0.0

    private var idList = ArrayList<String>()
    var arrayList = ArrayList<String>()
    private lateinit var unique_id: String
    private lateinit var plot_no: String
    private lateinit var area: String
    private lateinit var name: String
    private lateinit var reason_id: String
    private lateinit var farmer_id: String
    private var token: String = ""

    lateinit var uri: Uri
    lateinit var photoPath: File
    lateinit var currentPhotoPath: String
    var imageFileName: String = ""
    var rotate = 0

    val watermark: Common = Common()

    private var img1: String = ""
    private var img2: String = ""

    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var ivback : ImageView

    private lateinit var btnSubmit: Button

    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_reupload)
        progress = SweetAlertDialog(this@ImageReuploadActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("unique_id")!!
            plot_no = bundle.getString("plot_no")!!
        } else {
            Log.e("unique_id", "Nope")
        }

        image1 = findViewById(R.id.image_re_upload_1)
        image2 = findViewById(R.id.image_re_upload_2)

        btnSubmit = findViewById(R.id.re_upload_image_submit)
        btnSubmit.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@ImageReuploadActivity, SweetAlertDialog.WARNING_TYPE)

            if (img1.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.preview_land1_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else{
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                arrayList.add(img1)

                if(img2.isEmpty()){
                    sendData(arrayList)
                }
                else {
                    arrayList.add(img2)
                    sendData(arrayList)
                }
            }
        })

        ivback = findViewById(R.id.ImageRe_back)
        ivback.setOnClickListener { onBackPressed() }


        image1.setOnClickListener(View.OnClickListener {
            progress.progressHelper.barColor = Color.parseColor("#06c238")
            progress.titleText = resources.getString(R.string.loading)
            progress.contentText = "Getting current location"
            progress.setCancelable(false)
            progress.show()

            imageLocation(1)
        })

        image2.setOnClickListener(View.OnClickListener {
            progress.progressHelper.barColor = Color.parseColor("#06c238")
            progress.titleText = resources.getString(R.string.loading)
            progress.contentText = "Getting current location"
            progress.setCancelable(false)
            progress.show()

            imageLocation(2)
        })

        checkGPS()
        getRejectedReason(unique_id, plot_no)
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
                        resolvableApiException.startResolutionForResult(this@ImageReuploadActivity, MY_PERMISSIONS_REQUEST_LOCATION)
                    } catch (sendIntentException: SendIntentException) {
                        sendIntentException.printStackTrace()
                    }
                }
                if (e.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(this@ImageReuploadActivity, "setting not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun sendData(arrayList: ArrayList<String>) {
        if (arrayList.isEmpty()) {
            progress.dismiss()
            nextScreen()
        }
        else {
            val currentUrl: String = arrayList[0]
            val file = File(currentUrl)

            val farmer_unique_id = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
            val farmer =  farmer_id.toRequestBody("text/plain".toMediaTypeOrNull())
            val plot = plot_no.toRequestBody("text/plain".toMediaTypeOrNull())
            val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val farmerUniqueID: MultipartBody.Part = MultipartBody.Part.createFormData("unique", null, farmer_unique_id)
            val farmerID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_id", null, farmer)
            val plotNumber: MultipartBody.Part = MultipartBody.Part.createFormData("plotno", null, plot)
            val body: MultipartBody.Part = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
            retIn.reUploadImage("Bearer $token", farmerUniqueID, farmerID, plotNumber, body)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.code() == 200){
                            arrayList.removeAt(0)

                            Log.e("response_code", response.code().toString())
                            sendData(arrayList)
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
    }

    private fun nextScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@ImageReuploadActivity, SweetAlertDialog.SUCCESS_TYPE)
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

    private fun getRejectedReason(unique_id: String, plot_no: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.rejectReason("Bearer $token" , unique_id, plot_no).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null){
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonObject1 = jsonObject.optJSONObject("plot")
                        val farmer_uniqueId = jsonObject1.optString("farmer_uniqueId")
                        val actual_owner_name = jsonObject1.optString("actual_owner_name")
                        val area_in_acers = jsonObject1.optString("area_in_acers")
                        val farmer = jsonObject1.optString("farmer_id")

                        area = area_in_acers
                        name = actual_owner_name
                        farmer_id = farmer

                        val jsonObject2 = jsonObject1.optJSONObject("reasons")
                        reason_id=  jsonObject2.optString("id")

                        val jsonArray = jsonObject.optJSONArray("Image")
                        for (i in 0 until jsonArray.length()){
                            val jsonObject = jsonArray.getJSONObject(i)
                            idList.add(jsonObject.optString("id"))
                        }
                    }
                }
                else{
                    Log.e("response_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }


    var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }

                Log.e("rotate", rotate.toString())
                image1.setImageBitmap(watermark.addWatermark(application.applicationContext, image, "#$unique_id | P$plot_no | $timeStamp | $lat1 | $lng1"))
                image1.rotation = rotate.toFloat()

                try {
                    val draw = image1.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    img1 = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var resultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//                val resizeBitmap =  resize(image, image.width / 2, image.height / 2)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }
                Log.e("rotate", rotate.toString())
                image2.setImageBitmap(watermark.addWatermark(application.applicationContext, image, "#$unique_id | P$plot_no | $timeStamp | $lat2 | $lng2"))
                image2.rotation = rotate.toFloat()

                try {
                    val draw = image2.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    img2 = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    override fun onLocationChanged(location: Location) {

    }

    private fun imageLocation(requestCode: Int) {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f,
            object : LocationListener {
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onLocationChanged(location: Location) {
                    Log.i("here--here", location.toString())
                    locationManager!!.removeUpdates(this)

                    cameraOpenStatus(requestCode, location.latitude, location.longitude)
                }
            })
    }

    private fun cameraOpenStatus(requestCode: Int, latitude: Double, longitude: Double) {
        if (requestCode == 1){
            progress.dismiss()

            val df = DecimalFormat("#.#####")
            lat1 = df.format(latitude).toDouble()
            lng1 = df.format(longitude).toDouble()

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {

                try {
                    photoPath = createImageFile()
                } catch (ex: IOException) {
                }

                // Continue only if the File was successfully created
                if (photoPath != null) {
                    uri = FileProvider.getUriForFile(
                        this@ImageReuploadActivity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoPath
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    resultLauncher1.launch(intent)
                }
            }
        }
        else{
            progress.dismiss()

            val df = DecimalFormat("#.#####")
            lat2 = df.format(latitude).toDouble()
            lng2 = df.format(longitude).toDouble()

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {

                try {
                    photoPath = createImageFile()
                } catch (ex: IOException) { }
                // Continue only if the File was successfully created
                if (photoPath != null) {
                    uri = FileProvider.getUriForFile(
                        this@ImageReuploadActivity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoPath
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    resultLauncher2.launch(intent)
                }
            }
        }
    }

}