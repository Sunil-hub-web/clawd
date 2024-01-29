package com.pnpawd.userapp.utils

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.R
import com.kyanogen.signatureview.SignatureView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignatureActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var clear: Button
    private lateinit var save: Button
    private lateinit var signatureView: SignatureView
    private lateinit var path: String

    lateinit var currentPhotoPath: String
    var imageFileName: String = ""
    lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        signatureView = findViewById(R.id.signature_view)
        clear = findViewById(R.id.clear)
        save = findViewById(R.id.save)

        clear.setOnClickListener { signatureView.clearCanvas() }

        save.setOnClickListener(View.OnClickListener {
            bitmap = signatureView.signatureBitmap
            path = saveImage(bitmap)


            Handler(Looper.getMainLooper()).postDelayed({
                signatureView.clearCanvas()

                val intent = Intent()
                intent.putExtra("path", path)
                setResult(RESULT_OK, intent)
                finish()

            }, 2000)
        })
    }

    private fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = createImageFile()

        uri = FileProvider.getUriForFile(
            this@SignatureActivity,
            BuildConfig.APPLICATION_ID + ".provider",
            wallpaperDirectory
        )

        try {
            val fileOutput = FileOutputStream(wallpaperDirectory)
            fileOutput.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this@SignatureActivity,
                arrayOf(wallpaperDirectory.path),
                arrayOf("image/jpeg"),
                null
            )
            fileOutput.close()
            Log.d("TAG", "File Saved::--->" + wallpaperDirectory.absolutePath)
            Toast.makeText(applicationContext, "Signature Saved !!!", Toast.LENGTH_SHORT).show()

            return wallpaperDirectory.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

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