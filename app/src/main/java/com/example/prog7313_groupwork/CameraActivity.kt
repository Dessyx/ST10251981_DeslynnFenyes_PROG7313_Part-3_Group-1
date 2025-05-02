package com.example.prog7313_groupwork

// import
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.prog7313_groupwork.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// -------------------------- Functionality of camera --------------------------------------
// Note that AI was used for the entirety of this class.
// It was used to assist in debugging and providing working camera functionality.
class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderResult = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Cannot take a photo without camera permissions", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        cameraProviderResult.launch(android.Manifest.permission.CAMERA)

        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            getExternalFilesDir(null),
            "expense_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            imgCaptureExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exception.message}")
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, "Failed to capture photo", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: photoFile.toUri()
                    Log.d("CameraActivity", "Photo saved to $savedUri")
                    
                    val resultIntent = Intent()
                    resultIntent.putExtra("image_path", photoFile.absolutePath)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        )
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraActivity", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        imgCaptureExecutor.shutdown()
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------