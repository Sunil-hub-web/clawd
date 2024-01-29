package com.pnpawd.userapp.pipeinstallation.Submitted

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.adapters.Pipe_Image_Adapter
import com.pnpawd.userapp.models.PipeImageModel
import com.pnpawd.userapp.models.PipeQtyModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.pnpawd.userapp.R
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
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
import kotlin.collections.ArrayList

class LandInfoSubmittedPreviewActivity : AppCompatActivity(), LocationListener {
    var PERMISSION_ALL = 1
    private var Permissions: Array<String> = arrayOf<String>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var model: ArrayList<PipeImageModel> = ArrayList<PipeImageModel>()
    private var poly_list = ArrayList<String>()
    var arrayList = ArrayList<String>()
    var LATLNG = ArrayList<LatLng>()
    var poly_list_LATLNG = ArrayList<LatLng>()
    private val bool = arrayOf("--Select--", "Yes", "No")
//    private lateinit var spinnerPipes: Spinner
    private lateinit var txtPlot_Area: TextView
    private lateinit var txtFarmer_name: TextView
//    private lateinit var txtpipe_req: TextView
//    private lateinit var txtAvailable: EditText
    private lateinit var txtPlotNumer: TextView

    private lateinit var btnBack : Button
    private lateinit var btnSubmit : Button

    private lateinit var image_preview_recyclerView: RecyclerView
    private lateinit var pipe_Image_Adapter: Pipe_Image_Adapter

    private lateinit var currentPhotoPath: String
    private lateinit var photoPath: File
    private lateinit var uri: Uri
    var imageFileName: String = ""
    var token: String = ""
    var  required_pipes: String = ""
    private var farmer_plot_uniqueid: String = ""

    private var imageModelPath: String = ""
    private var farmer_name: String = ""
    var imageLat: String = ""
    var imageLng: String = ""
    var minum: String = ""
    var MIN: String = ""

    val watermark: Common = Common()
    private lateinit var locationManager: LocationManager

    private var distanceList = ArrayList<String>()
    private var countList = ArrayList<String>()
    private lateinit var area: String
    private lateinit var unique_id: String
    private lateinit var plot_no: String
    private lateinit var farmer_id: String

    lateinit var linearList: LinearLayout
    var i: Int = 0
    var indx: Int = 0
    var rotate = 0
    var lkjs: Int = 0
    var pipePosition: Int = 0
    var required: Boolean = false

    private lateinit var progress: SweetAlertDialog

    private var numberList = ArrayList<Int>()
    private var Polygon_lat_lng = ArrayList<LatLng>()
    private var ImageLinks = ArrayList<String>()
    private var distanceInMeters = ArrayList<String>()
    private var pipeNoList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_land_info_preview)
        progress = SweetAlertDialog(this@LandInfoSubmittedPreviewActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

//        spinnerPipes = findViewById(R.id.rPipes)
        txtPlot_Area = findViewById(R.id.tvPlotArea)
//        txtpipe_req = findViewById(R.id.tvPipes)
//        txtAvailable = findViewById(R.id.spPlotUniqueId)
        txtFarmer_name = findViewById(R.id.pipe_farmer_full_name)
        txtPlotNumer = findViewById(R.id.pipe_plot_number)

        btnBack = findViewById(R.id.land_Info_Pre_Btn_Back)
        btnSubmit = findViewById(R.id.land_Info_Pre_Btn_Submit)

        image_preview_recyclerView = findViewById(R.id.ImagePreviewPipeInstallation)
        linearList = findViewById(R.id.layout_list)


// Getting value from previous activity
        val bundle = intent.extras
        if (bundle != null) {
            poly_list = bundle.getStringArrayList("polygon_lat_lng")!!
            farmer_id = bundle.getString("farmer_id").toString()
            unique_id = bundle.getString("unique_id").toString()
            plot_no = bundle.getString("sub_plot_no").toString()
            area = bundle.getString("area").toString()
            farmer_name = bundle.getString("farmer_name").toString()

            txtPlot_Area.text = area
            txtFarmer_name.text = farmer_name

// Putting the polygon plotted lat & lng in an array
            for(i in 0 until poly_list.size){
                val dfgdg: String =  poly_list[i].replace("[^0-9,.]".toRegex(), "")
                val lat: Double = dfgdg.split(",").first().toDouble()
                val lng: Double = dfgdg.split(",").last().toDouble()

                poly_list_LATLNG.add(LatLng(lat, lng))
            }

// Api call to get the total pipe quantity.
            getPipeQty(unique_id, plot_no)
        } else {
            Log.e("total_plot", "Nope")
        }

// Initializing and connecting the recyclerview with adapter for images.
        pipe_Image_Adapter = Pipe_Image_Adapter(model)
        val layoutManager = LinearLayoutManager(this)
        image_preview_recyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        image_preview_recyclerView.adapter = pipe_Image_Adapter


// Spinner connect with adapter and adding value to it.
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bool)
//        spinnerPipes.adapter = adapter
//        spinnerPipes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                linearList.isVisible = position == 1
//                pipePosition = position
//
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }

// Submit click listener.
        btnSubmit.setOnClickListener {
            val WarningDialog =
                SweetAlertDialog(this@LandInfoSubmittedPreviewActivity, SweetAlertDialog.WARNING_TYPE)

            if (imageFileName == "") {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Click Image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
//            }else if(pipePosition == 0){
//                WarningDialog.titleText = resources.getString(R.string.warning)
//                WarningDialog.contentText = "Select valid option from Are you installing pipe"
//                WarningDialog.confirmText = resources.getString(R.string.ok)
//                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else {
                checkIfValidAndRead()
            }
        }


// Back click listener.
        btnBack.setOnClickListener { super.onBackPressed() }

        Permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!hasPermissions(this, *Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, PERMISSION_ALL)
        }

// Initializing FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }



//  Adding distance & count to an array.
    private fun checkIfValidAndRead() {
    distanceList.clear()
    countList.clear()
    lkjs = 0

        for (i in 0 until linearList.childCount) {
            val cricketerView: View = linearList.getChildAt(i)

            val editdistance = cricketerView.findViewById<View>(R.id.distance) as TextView
            val editCount = cricketerView.findViewById<View>(R.id.count) as TextView


            if (editdistance.text.toString() != "") {
//                Log.e("plotName", editdistance.text.toString())
                distanceList.add(editdistance.text.toString())
            } else {
                break
            }


            if (editCount.text.toString() != "") {
//                Log.e("areaHectare", editCount.text.toString())
                countList.add(editCount.text.toString())
            }
            else {
                break
            }
        }


// Checking if the distance is less than || greater than 10.
        first@ for (i in 0 until distanceList.size) {
            val plotDouble = distanceList[i].toDouble()
            if (plotDouble < 10 && countList[i] != "1") {
                Toast.makeText(this@LandInfoSubmittedPreviewActivity, "$plotDouble", Toast.LENGTH_SHORT).show()
                Log.e("sdfkfjs", "fjksdfskdf")

                val WarningDialog = SweetAlertDialog(this@LandInfoSubmittedPreviewActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Distance cannot be less \n than ${10} meters"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    Log.e("Next Screen", lkjs.toString())
                    WarningDialog.cancel()
                }.show()

                lkjs = 1
                Log.e("Next Screen", lkjs.toString())
                break@first
            }
        }

        Log.e("Next Screen", lkjs.toString())
        if (lkjs == 0){
            Log.e("insidesetdata", "fjksdfskdf")
// If the distance is less than than 10.
            sendData(LATLNG)
        }
    }


// For loop to send data.
    private fun sendData(arrayList: ArrayList<LatLng>) {

        if (arrayList.size == Polygon_lat_lng.size) {
            nextScreen()
        }
        else {
            progress.progressHelper.barColor = Color.parseColor("#06c238")
            progress.titleText = resources.getString(R.string.loading)
            progress.contentText = resources.getString(R.string.data_send)
            progress.setCancelable(false)
            progress.show()

// Getting lat & lng from arraylist.
            val currentLatLng: LatLng = arrayList[Polygon_lat_lng.size]
            Log.e("sendData_length", currentLatLng.latitude.toString())

// Getting Distance from arraylist.
            val distance = distanceList[Polygon_lat_lng.size]
            Log.e("distance", distance)

// Getting Count from arraylist.
            val no = countList[Polygon_lat_lng.size]
            Log.e("count", no)

// Getting Images from ImageModel.
            val imageIndex = model[0].getPath()
            Log.e("imageIndex", imageIndex)
            val file = File(imageIndex)

            val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val farmer_id = farmer_id.toRequestBody("text/plain".toMediaTypeOrNull())
            val farmer_unique_id = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
            val plot_no = plot_no.toRequestBody("text/plain".toMediaTypeOrNull())
            val area = distance.toRequestBody("text/plain".toMediaTypeOrNull())
            val pipe_count = no.toRequestBody("text/plain".toMediaTypeOrNull())
            val lat = currentLatLng.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lng = currentLatLng.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val farmerPlotUniqueid = farmer_plot_uniqueid.toRequestBody("text/plain".toMediaTypeOrNull())
//            val pipes_required = txtpipe_req.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
//            val pipe_available = txtAvailable.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val installation = bool[pipePosition].toRequestBody("text/plain".toMediaTypeOrNull())

            val body: MultipartBody.Part = MultipartBody.Part.createFormData("images", file.name, requestFile)
            val UniqueID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_id", null, farmer_id)
            val FarmerUniqueID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, farmer_unique_id)
            val plot_NO: MultipartBody.Part = MultipartBody.Part.createFormData("plot_no", null, plot_no)
            val Distance: MultipartBody.Part = MultipartBody.Part.createFormData("distance", null, area)
            val pipe_no: MultipartBody.Part = MultipartBody.Part.createFormData("pipe_no", null, pipe_count)
            val latitude: MultipartBody.Part = MultipartBody.Part.createFormData("lat", null, lat)
            val longitude: MultipartBody.Part = MultipartBody.Part.createFormData("lng", null, lng)
            val FarmerPlotUniqueid: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_plot_uniqueid", null, farmerPlotUniqueid)
//            val no_pipe_req : MultipartBody.Part = MultipartBody.Part.createFormData("no_pipe_req", null, pipes_required)
//            val no_pipe_avl : MultipartBody.Part = MultipartBody.Part.createFormData("no_pipe_avl", null, pipe_available)
            val installing_pipe : MultipartBody.Part = MultipartBody.Part.createFormData("installing_pipe", null, installation)

// Sending the data in multipart form.
            val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
            apiInterface.sendPipeLastData("Bearer $token", UniqueID, FarmerUniqueID,  FarmerPlotUniqueid, plot_NO,
                latitude, longitude, pipe_no, Distance, installing_pipe, body).enqueue(object:
                Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200){
                        model.removeAt(0)
                        distanceList.removeAt(Polygon_lat_lng.size)
                        countList.removeAt(Polygon_lat_lng.size)
                        arrayList.removeAt(Polygon_lat_lng.size)

                        sendData(arrayList)
                    }
                    else if(response.code() == 500){
                        Log.e("response.code", response.code().toString())
                    }
                    else{
                        Log.e("response.code", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                    progress.dismiss()
                }
            })
        }
    }

// Code to get to the next screen.
    private fun nextScreen() {
        progress.dismiss()
        val SuccessDialog = SweetAlertDialog(this@LandInfoSubmittedPreviewActivity, SweetAlertDialog.SUCCESS_TYPE)

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

// Code to be called to dynamically display the Data filling view of click camera, distance.
    private fun addView(position: Int) {
        if (position != 0) {
            val plotView = layoutInflater.inflate(R.layout.pipe_image_row, null, false)
            val linear = plotView.findViewById<View>(R.id.linearColor) as LinearLayout
            val camera = plotView.findViewById<View>(R.id.camera) as ImageView
            val item = plotView.findViewById<View>(R.id.count) as TextView
            val distance = plotView.findViewById<View>(R.id.distance) as TextView

            i++
            item.text = i.toString()
            linear.setBackgroundColor(Color.BLUE)

            camera.setOnClickListener(View.OnClickListener {
                checkData(item.text.toString())
            })
            linearList.addView(plotView)
            addView(position - 1)
        }
        else{
            Log.e("Polygon_lat_lng.size", Polygon_lat_lng.size.toString())
           fillData(Polygon_lat_lng.size)
        }
    }

// If already data is present then add it to the dynamic view.
    private fun fillData(size: Int) {
        Log.e("SSSize", size.toString())
        for (i in 0 until size){
            val view: View = linearList.getChildAt(i)
            val linear = view.findViewById<View>(R.id.linearColor) as LinearLayout
            val editCamera = view.findViewById<View>(R.id.camera) as ImageView
            val editCount = view.findViewById<View>(R.id.count) as TextView
            val editDistance = view.findViewById<View>(R.id.distance) as TextView

            editDistance.text = distanceInMeters[i]
            linear.setBackgroundColor(Color.parseColor("#06c238"))

            editCamera.setOnClickListener(View.OnClickListener {
                if(distanceInMeters[i].isNotEmpty()){

                    imageSubmitted()
//                    Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Image Already Submitted", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Image", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun imageSubmitted() {
        val WarningDialog = SweetAlertDialog(this@LandInfoSubmittedPreviewActivity, SweetAlertDialog.WARNING_TYPE)
        WarningDialog.titleText = resources.getString(R.string.warning)
        WarningDialog.contentText = "Image Already Submitted"
        WarningDialog.confirmText = resources.getString(R.string.ok)
        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
    }

    // Here it checks which image is already taken and displays the appropriate message.
    private fun checkData(MINUSINDEX: String) {
        val view: View = linearList.getChildAt(MINUSINDEX.toInt() - 1)
        val editCamera = view.findViewById<View>(R.id.camera) as ImageView
        val editCount = view.findViewById<View>(R.id.count) as TextView
        val editDistance = view.findViewById<View>(R.id.distance) as TextView


        if (model.size != 0) {
            if (LATLNG.size + 1 < MINUSINDEX.toInt()){
                Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Click above image first", Toast.LENGTH_SHORT).show()
            }
            else {
                minum = MINUSINDEX

                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.location)
                progress.setCancelable(false)
                progress.show()

                getActualLocation(MINUSINDEX)
            }
        }
        else{
            if (MINUSINDEX == (pipeNoList.size + 1).toString()) {
                minum = MINUSINDEX

                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.location)
                progress.setCancelable(false)
                progress.show()

                getActualLocation(MINUSINDEX)
//                Log.e("LATLNG", MINUSINDEX.toInt().toString())
//                Log.e("LATLNG", (pipeNoList.size + 1).toString())
            }
            else{
                Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Click first image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

// Getting the current location while clicking the image.
    private fun getActualLocation(MINUSINDEX: String){
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),  101)
        return
    }

        MIN = MINUSINDEX
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            requestNewLocationData()

        }
    }

    // Code to take image from camera.
    private fun openCamera(position: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            try {
                photoPath = createImageFile()
            } catch (ex: IOException) {}
// Continue only if the File was successfully created
            if (photoPath != null) {
                uri = FileProvider.getUriForFile(
                    this@LandInfoSubmittedPreviewActivity,
                    BuildConfig.APPLICATION_ID + ".provider", photoPath
                )
                indx = position.toInt() - 1

                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                resultLauncher1.launch(intent)
                Log.e("Camera_function", "Camera_function")

                calculation()
            }
        }
    }

// Calculate the distance between two locations.
    private fun calculation() {
        val view: View = linearList.getChildAt(minum.toInt() -1)
        val editCamera = view.findViewById<View>(R.id.camera) as ImageView
        val editCount = view.findViewById<View>(R.id.count) as TextView
        val editDistance = view.findViewById<View>(R.id.distance) as TextView

//        Log.e("calculation", LATLNG[LATLNG.size - 1].toString())

        if(minum.toInt() == pipeNoList.size + 1){
            val df = DecimalFormat("#####")
//            Log.e("calculation1", Polygon_lat_lng[pipeNoList.size -1].toString())
//            Log.e("calculation2",LatLng(imageLat.toDouble(), imageLng.toDouble()).toString())
            val distance = df.format(SphericalUtil.computeDistanceBetween(Polygon_lat_lng[pipeNoList.size -1], LatLng(imageLat.toDouble(), imageLng.toDouble())))
            editDistance.text = distance.toString()
        }
        else{
            val df = DecimalFormat("#####")
            Log.e("calculation1", LATLNG[minum.toInt() - 1].toString())
            Log.e("calculation2", LATLNG[minum.toInt() - 2].toString())
            val distance = df.format(SphericalUtil.computeDistanceBetween(LATLNG[minum.toInt() - 1], LATLNG[minum.toInt() - 2]))
            editDistance.text = distance.toString()
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


// Code that is called when we are clicking the image.
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
                val edittedImage = watermark.addWatermark(application.applicationContext, image, "#$unique_id | P$plot_no | $timeStamp | $imageLat | $imageLng")

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
                    imageModelPath = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }

                for (i in model.indices){
                    if (model[i].getIndex() == indx){
                        Log.e("indices", indx.toString())
                        model[i] = PipeImageModel(edittedImage, rotate, indx, imageModelPath)
                        pipe_Image_Adapter.notifyDataSetChanged()

                        required = true
                        Log.e("required", required.toString())
                    }
                }

                if(!required){
                    model.add(PipeImageModel(edittedImage, rotate, indx, imageModelPath))
                    pipe_Image_Adapter = Pipe_Image_Adapter(model)
                    image_preview_recyclerView.adapter = pipe_Image_Adapter
                    pipe_Image_Adapter.notifyDataSetChanged()

                    required = false
                    Log.e("required", required.toString())
                }

                required = false

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


// Making Api call to get pipe quantity.
    private fun getPipeQty(unique_id: String, plot_no: String) {
        val pipeQtyModel = PipeQtyModel(unique_id, plot_no)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.getPipeQty("Bearer $token", pipeQtyModel).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        required_pipes = stringResponse.optString("required_pipes")
                        farmer_plot_uniqueid = stringResponse.optString("farmer_plot_uniqueid")
                        txtPlotNumer.text = farmer_plot_uniqueid

//                        txtpipe_req.text = required_pipes
//                        txtAvailable.text = required_pipes

                        alreadySubmittedData(required_pipes.toInt())
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }


// Making Api call to get already submitted data.
    private fun alreadySubmittedData(toInt: Int) {

    Log.e("Checking_Pipe_Data", "Checking_Pipe_Data")

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkPipeData("Bearer $token", unique_id, farmer_plot_uniqueid, plot_no).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if(response.body() != null){
                        val stringResponse = JSONObject(response.body()!!.string())
                        val data = stringResponse.getJSONObject("data")

                        val locationArray = data.optJSONArray("pipes_location")
                        for (i in 0 until locationArray.length()) {
                            val jsonObject = locationArray.getJSONObject(i)
                            val lat = jsonObject.optString("lat")
                            val lng = jsonObject.optString("lng")
                            val pipe_no = jsonObject.optString("pipe_no")
                            val imageURL = jsonObject.optString("images")
                            val distance = jsonObject.optString("distance")

                            val latLng = LatLng(lat.toDouble(), lng.toDouble())
//                            Log.e("latLng", latLng.toString())

                            Polygon_lat_lng.add(latLng)
                            LATLNG.add(latLng)
                            ImageLinks.add(imageURL)
                            pipeNoList.add(pipe_no)
                            distanceInMeters.add(distance)
                        }

                        Log.e("Checking_Pipe_Data", "Checking_Pipe_Data")
                        addView(toInt)

                    }
                }
                else if (response.code() == 422){
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    @SuppressLint("LongLogTag")
    override fun onLocationChanged(location: Location) {
        val df = DecimalFormat("#.#####")
        imageLat = df.format(location.latitude).toString()
        imageLng = df.format(location.longitude).toString()

        Log.e("onLocationChanged_LATLNG ", LATLNG.size.toString())
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


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(mlocationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            Log.e("mLastLocation.latitude", mLastLocation?.latitude.toString())
            Log.e("mLastLocation.longitude", mLastLocation?.longitude.toString())

            imageLat = mLastLocation?.latitude.toString()
            imageLng = mLastLocation?.longitude.toString()

            stopAgain(mLastLocation)
        }
    }

    private fun stopAgain(it: Location?) {
        Log.e("Stopped", "Location Update Stopped")
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)

        val df = DecimalFormat("#.#####")
        imageLat = df.format(it?.latitude).toString()
        imageLng = df.format(it?.longitude).toString()

        val latLng = LatLng(it!!.latitude, it!!.longitude)

        progress.dismiss()

        val valid = PolyUtil.containsLocation(latLng, poly_list_LATLNG, false)
        Log.e("validLocation ", valid.toString())

        if(valid){
            if (LATLNG.size == 0) {
                LATLNG.add(latLng)

                Log.e("LATLNG ", it.toString())
                openCamera(MIN)
            } else {
                val validLocation = PolyUtil.containsLocation(latLng, poly_list_LATLNG, false)
                Log.e("validLocation ", validLocation.toString())
                if (validLocation) {
                    if (LATLNG.size < minum.toInt()) {
                        LATLNG.add(latLng)

                        Log.e("LATLNG ", it.toString())
                        openCamera(MIN)
                    } else {
                        LATLNG[minum.toInt() - 1] = latLng

                        Log.e("LATLNG ", it.toString())
                        openCamera(MIN)
                    }
                } else {
                    Toast.makeText(
                        this@LandInfoSubmittedPreviewActivity,
                        "Point out-side polygon",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        else{
            Toast.makeText(
                this@LandInfoSubmittedPreviewActivity,
                "Point out-side polygon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@LandInfoSubmittedPreviewActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@LandInfoSubmittedPreviewActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@LandInfoSubmittedPreviewActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermissions(context: Context?, vararg PERMISSIONS: String): Boolean {
        if (context != null) {
            for (permissions in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permissions
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

}