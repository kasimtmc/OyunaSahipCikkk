package com.kasimtmc.oyunasahipcik

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.kasimtmc.oyunasahipcik.Constants.TAG
import com.kasimtmc.oyunasahipcik.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private var selectCamera= CameraSelector.DEFAULT_BACK_CAMERA
    private var cameratype = 1
    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        outputDirectory= getOutputDirectory()

        if (allPermissionGranted()) {
            startCameraMain()
            binding.switchButton.setOnClickListener {
                if (cameratype== 1) {
                    startCameraFront()
                    cameratype= 2
                } else if (cameratype== 2){
                    startCameraMain()
                    cameratype= 1
                } else {
                    println("error!")
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, Constants.REQUIRED_PERMISSIONS, Constants.REQUEST_CODE_PERMISSIONS)
        }

        binding.takePhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir= externalCacheDirs.firstOrNull()?.let {mediaFile ->
            File(mediaFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }

        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto() {
        val imageCapture= imageCapture ?: return
        val photoFile = File(outputDirectory, SimpleDateFormat(Constants.FILE_NAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())+".jpg")
        val outputOption= ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOption, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri= Uri.fromFile(photoFile)
                    val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                    val myDir= File("${root}/OyunaSahipCik")
                    if (!myDir.exists()) {
                        myDir.mkdirs()
                    }
                    val fileName= "${SimpleDateFormat(Constants.FILE_NAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())}.jpg"
                    val myFile= "${myDir}/${fileName}"
                    val sourcePath = Paths.get(photoFile.toURI())
                    val targetPath = Paths.get(myFile)
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)

                    val intent= Intent(this@MainActivity, EditActivity::class.java)
                    intent.putExtra("imagepath", fileName)
                    startActivity(intent)
                    finish()
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "onError: ${exception.message}", exception)
                }

            })
    }

    private fun startCameraMain() {
        val cameraProviderFeature= ProcessCameraProvider.getInstance(this)
        cameraProviderFeature.addListener({
            val cameraProvider: ProcessCameraProvider= cameraProviderFeature.get()

            val preview= Preview.Builder().build().also {mPreview ->
                mPreview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture= ImageCapture.Builder().build()
            val cameraSelector= CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.d(TAG, "startCamera Fail:", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startCameraFront() {
        val cameraProviderFeature= ProcessCameraProvider.getInstance(this)
        cameraProviderFeature.addListener({
            val cameraProvider: ProcessCameraProvider= cameraProviderFeature.get()

            val preview= Preview.Builder().build().also {mPreview ->
                mPreview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture= ImageCapture.Builder().build()
            val cameraSelector= CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.d(TAG, "startCamera Fail:", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCameraMain()
            } else {
                Toast.makeText(this, "Permissions Not Granted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }

}