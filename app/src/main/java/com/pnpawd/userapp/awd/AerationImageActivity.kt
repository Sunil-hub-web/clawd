package com.pnpawd.userapp.awd

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat
import cn.pedant.SweetAlert.BuildConfig
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.pnpawd.userapp.SessionManager
import com.pnpawd.userapp.ViewCameraDetail
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

class AerationImageActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var txtMobile: TextView
    private lateinit var txtUniqueId: TextView
    private lateinit var txtSubPlot: TextView
    private lateinit var txtName: TextView
    private lateinit var txtAeriation: TextView
    private lateinit var txtPipeNumber: TextView

    private lateinit var back: Button
    private lateinit var submit: Button

    private lateinit var currentPhotoPath: String
    private lateinit var photoPath: File
    private lateinit var uri: Uri
    var imageFileName: String = ""
    private var imageModelPath1: String = ""
    private var imageModelPath2: String = ""
    private var rotate = 0
    val watermark: Common = Common()

    private var imageLat: String = ""
    private var imageLng: String = ""
    private var token: String = ""
    private lateinit var farmer_plot_uniqueid: String
    private lateinit var pipe_no: String
    private lateinit var unique_id: String
    private lateinit var aeriation: String
    private lateinit var farmer_name: String
    private lateinit var mobile_number: String
    private var pipeImageLatitude = 0.0
    private var pipeImageLongitude = 0.0
    private var imageNumber = 0
    private var pipe_installation_id = 0
    private var plot_no: Int = 0
    private lateinit var locationManager: LocationManager

    var imagePathList = ArrayList<String>()
    var arrayList = ArrayList<String>()
    var pipeImageLocation = ArrayList<String>()

    private lateinit var progress: SweetAlertDialog

    lateinit var sessionManager: SessionManager

    var dataimagelist = "dataimage";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aeration_image)
        progress = SweetAlertDialog(this@AerationImageActivity, SweetAlertDialog.PROGRESS_TYPE)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        image1 = findViewById(R.id.aeration_camera_capture1)
        image2 = findViewById(R.id.aeration_camera_capture2)
        txtMobile = findViewById(R.id.aeration_mobile)
        txtUniqueId = findViewById(R.id.aeriation_uniqueId)
        txtSubPlot = findViewById(R.id.aeration_sub_plots)
        txtName = findViewById(R.id.aeration_image_farmer_name)
        txtAeriation = findViewById(R.id.aeration_image_event_number)
        txtPipeNumber = findViewById(R.id.aeriation_pipe_number)

        back = findViewById(R.id.aeriation_image_back)
        submit = findViewById(R.id.aeriation_image_submit)

        sessionManager = SessionManager(this@AerationImageActivity)

        val bundle = intent.extras
        if (bundle != null) {
            pipeImageLocation = bundle.getStringArrayList("latlngList")!!
            pipeImageLatitude = bundle.getDouble("latitude")
            pipeImageLongitude = bundle.getDouble("longitude")
            farmer_plot_uniqueid = bundle.getString("farmer_plot_uniqueid").toString()
            pipe_no = bundle.getString("pipe_no")!!
            unique_id = bundle.getString("unique_id")!!
            aeriation = bundle.getString("aeriation")!!
            farmer_name = bundle.getString("farmer_name")!!
            mobile_number = bundle.getString("mobile_number")!!
            pipe_installation_id = bundle.getInt("pipe_installation_id")
            plot_no = bundle.getInt("plot_no")

            Log.e("pipe_installation_id", pipe_installation_id.toString())

            txtMobile.text = mobile_number
            txtUniqueId.text = unique_id
            txtSubPlot.text = farmer_plot_uniqueid
            txtName.text = farmer_name
            txtAeriation.text = aeriation
            txtPipeNumber.text = pipe_no
        } else {

        }

        image1.setOnClickListener(View.OnClickListener {
            //imageNumber = 1
           // getActualLocation(1)

            dataimagelist = ""
            val intent = Intent(this@AerationImageActivity, ViewCameraDetail::class.java)
            intent.putExtra("clickimage", "areationimage1")
            startActivity(intent)
        })

        image2.setOnClickListener(View.OnClickListener {
            //imageNumber = 2
            //getActualLocation(2)

            dataimagelist = ""
            val intent = Intent(this@AerationImageActivity, ViewCameraDetail::class.java)
            intent.putExtra("clickimage", "areationimage2")
            startActivity(intent)
        })

        submit.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@AerationImageActivity, SweetAlertDialog.WARNING_TYPE)

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
        gpsCheck()
    }

    private fun sendData(imagePathList: ArrayList<String>) {
        val pipeInstallationId =
            pipe_installation_id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueId = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val farmerPlotUniqueId =
            farmer_plot_uniqueid.toRequestBody("text/plain".toMediaTypeOrNull())
        val plotNo = plot_no.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val aeriationNo = aeriation.toRequestBody("text/plain".toMediaTypeOrNull())
        val pipeNo = pipe_no.toRequestBody("text/plain".toMediaTypeOrNull())

        val pipe_installation_id: MultipartBody.Part =
            MultipartBody.Part.createFormData("pipe_installation_id", null, pipeInstallationId)
        val farmer_uniqueId: MultipartBody.Part =
            MultipartBody.Part.createFormData("farmer_uniqueId", null, uniqueId)
        val farmer_plot_uniqueid: MultipartBody.Part =
            MultipartBody.Part.createFormData("farmer_plot_uniqueid", null, farmerPlotUniqueId)
        val plot_no: MultipartBody.Part = MultipartBody.Part.createFormData("plot_no", null, plotNo)
        val aeration_no: MultipartBody.Part =
            MultipartBody.Part.createFormData("aeration_no", null, aeriationNo)
        val pipe_no: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_no", null, pipeNo)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.sendAeriationData("Bearer $token", pipe_installation_id, farmer_uniqueId, farmer_plot_uniqueid, plot_no, aeration_no,
            pipe_no).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    sendImageData(imagePathList)
                } else if (response.code() == 422) {
                    alreadySubmitted()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AerationImageActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }

    private fun sendImageData(imagePathList: ArrayList<String>) {
        if (imagePathList.isEmpty()){
            homeScreen()
        }
        else{
            val currentUrl: String = imagePathList[0]
            val file = File(currentUrl)

            val pipeInstallationId = pipe_installation_id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val uniqueId = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
            val farmerPlotUniqueId = farmer_plot_uniqueid.toRequestBody("text/plain".toMediaTypeOrNull())
            val plotNo = plot_no.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val aeriationNo = aeriation.toRequestBody("text/plain".toMediaTypeOrNull())
            val pipeNo = pipe_no.toRequestBody("text/plain".toMediaTypeOrNull())
            val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val pipe_installation_id: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_installation_id", null, pipeInstallationId)
            val farmer_uniqueId: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, uniqueId)
            val farmer_plot_uniqueid: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_plot_uniqueid", null, farmerPlotUniqueId)
            val plot_no: MultipartBody.Part = MultipartBody.Part.createFormData("plot_no", null, plotNo)
            val aeration_no: MultipartBody.Part = MultipartBody.Part.createFormData("aeration_no", null, aeriationNo)
            val pipe_no: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_no", null, pipeNo)
            val body: MultipartBody.Part = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
            apiInterface.sendAeriationImage("Bearer $token", pipe_installation_id, farmer_uniqueId, farmer_plot_uniqueid, plot_no, aeration_no,
                pipe_no, body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200) {
                        imagePathList.removeAt(0)
                        sendImageData(imagePathList)
                    }
                    else{
                        Toast.makeText(this@AerationImageActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        progress.dismiss()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@AerationImageActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                    progress.dismiss()
                }
            })
        }
    }

    private fun alreadySubmitted() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@AerationImageActivity, SweetAlertDialog.WARNING_TYPE)
        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Data Already Submitted. "
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


    private fun gpsCheck() {
        val active = isLocationEnabled(this@AerationImageActivity)
        Log.e("active", active.toString())

        if (!active) {
            AlertDialog.Builder(this@AerationImageActivity)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.yes,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        this@AerationImageActivity.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    })
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
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

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            Log.e("mLastLocation.latitude", mLastLocation?.latitude.toString())
            Log.e("mLastLocation.longitude", mLastLocation?.longitude.toString())

            imageLat = mLastLocation?.latitude.toString()
            imageLng = mLastLocation?.longitude.toString()

            val df = DecimalFormat("#####")
            val distance = df.format(SphericalUtil.computeDistanceBetween(LatLng(imageLat.toDouble(), imageLng.toDouble()), LatLng(pipeImageLatitude, pipeImageLongitude)))

            if(distance.toDouble() < 30) {
                stop()
            }
            else{
                val WarningDialog = SweetAlertDialog(this@AerationImageActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Distance cannot be less than ${30} meters"
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
                val edittedImage = watermark.addWatermark(application.applicationContext, image, "#$timeStamp | $imageLat | $imageLng")
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
                val edittedImage = watermark.addWatermark(application.applicationContext, image, "#$timeStamp | $imageLat | $imageLng")
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
                    this@AerationImageActivity,
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun homeScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@AerationImageActivity, SweetAlertDialog.SUCCESS_TYPE)

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

    override fun onStart() {
        super.onStart()

        if (!sessionManager.getAERATIONIMAGE1().equals("DEFAULT")){
            imageModelPath1 = sessionManager.getAERATIONIMAGE1().toString()
            val imgBitmap = BitmapFactory.decodeFile(sessionManager.getAERATIONIMAGE1())
            image1.setImageBitmap(imgBitmap)
            Log.d("userdetailsdata123",imageModelPath1)
        }

        if (!sessionManager.getAERATIONIMAGE2().equals("DEFAULT")){
            imageModelPath2 = sessionManager.getAERATIONIMAGE2().toString()
            val imgBitmap = BitmapFactory.decodeFile(sessionManager.getAERATIONIMAGE2())
            image2.setImageBitmap(imgBitmap)
            Log.d("userdetailsdata145",imageModelPath1)
        }
    }

}