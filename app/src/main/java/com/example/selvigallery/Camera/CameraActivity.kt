package com.example.selvigallery.Camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.Surface
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.selvigallery.R
import com.example.selvigallery.Utils.StatusBarSetup
import com.example.selvigallery.Utils.setFadeClickListener
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageView
    private lateinit var flashButton: ImageView
    private lateinit var switchButton: ImageView

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraExecutor: ExecutorService

    private var flashEnabled = false

    // Request code for permissions
    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Setup status bar
        StatusBarSetup().setUpStatusBar(this)

        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        flashButton = findViewById(R.id.flashButton)
        switchButton = findViewById(R.id.switchButton)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Permission check
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        captureButton.setFadeClickListener {
            takePhoto()
        }

        flashButton.setFadeClickListener {
            flashEnabled = !flashEnabled
            camera?.cameraControl?.enableTorch(flashEnabled)
            // Update flash icon state
            flashButton.setImageResource(if (flashEnabled) R.drawable.baseline_bolt_24_yell else R.drawable.baseline_bolt_24)
        }

        switchButton.setFadeClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                // Omit setTargetRotation or set it dynamically to previewView.display.rotation
                // .setTargetRotation(previewView.display.rotation)
                .build()

            try {
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Reset flash status if enabled (and update button icon)
                camera?.cameraControl?.enableTorch(flashEnabled)
                flashButton.setImageResource(if (flashEnabled) R.drawable.baseline_bolt_24_yell else R.drawable.baseline_bolt_24)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // ** Reverting to previous file saving location **
        val photoFile = File(
            getExternalFilesDir("Photos"),
            "IMG_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Capture failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace() // Log the error for debugging
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Since savedUri can be null or not directly pointing to the file path here,
                    // we'll rely on photoFile.absolutePath directly for decoding.
                    val path = photoFile.absolutePath

                    val originalBitmap = BitmapFactory.decodeFile(path)

                    if (originalBitmap == null) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Photo saved, but failed to decode for enhancement.", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    // Adjust contrast and brightness
                    val adjustedBitmap = adjustContrastBrightness(originalBitmap, contrast = 0.85f, brightness = 15f)

                    if (adjustedBitmap == null) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Photo saved, but enhancement failed.", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    // Overwrite original file with adjusted bitmap (JPEG 100%)
                    try {
                        // Use FileOutputStream directly for the File object
                        FileOutputStream(photoFile).use { outStream ->
                            adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                            outStream.flush() // Ensure all data is written
                        }

                        runOnUiThread {
                            Toast.makeText(applicationContext, "Photo saved and enhanced!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (ex: Exception) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Saved but post-processing failed: ${ex.message}", Toast.LENGTH_LONG).show()
                            ex.printStackTrace() // Log the exception for debugging
                        }
                    }
                }
            }
        )
    }

    private fun adjustContrastBrightness(bitmap: Bitmap, contrast: Float, brightness: Float): Bitmap? {
        // Ensure the output bitmap has a mutable config for drawing
        val outputConfig = bitmap.config ?: Bitmap.Config.ARGB_8888
        val retBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, outputConfig)
        val canvas = Canvas(retBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return retBitmap
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}


