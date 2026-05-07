package com.mutia.deteksistrawberry

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class home : Fragment() {

    private lateinit var imgPreview: ImageView
    private lateinit var cameraPreview: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var yoloDetector: YoloDetector

    private var isRealtimeActive = false

    // 📷 Kamera
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val bitmap = it.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    showImagePreview()
                    imgPreview.setImageBitmap(bitmap)
                }
            }
        }

    // 🖼️ Galeri
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                showImagePreview()
                imgPreview.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        imgPreview = view.findViewById(R.id.imgPreview)
        cameraPreview = view.findViewById(R.id.cameraPreview)
        yoloDetector = YoloDetector(requireContext())

        val btnCamera = view.findViewById<MaterialButton>(R.id.btnCamera)
        val btnGallery = view.findViewById<MaterialButton>(R.id.btnGallery)
        val btnRealtime = view.findViewById<MaterialButton>(R.id.btnRealtime)
        val btnAnalisis = view.findViewById<MaterialButton>(R.id.btnAnalisis)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 📷 Kamera
        btnCamera.setOnClickListener {
            stopRealtime()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

        // 🖼️ Galeri
        btnGallery.setOnClickListener {
            stopRealtime()
            galleryLauncher.launch("image/*")
        }

        // 🎥 Realtime Kamera
        btnRealtime.setOnClickListener {
            val intent = Intent(requireContext(), RealtimeCameraActivity::class.java)
            startActivity(intent)
        }

        // 🔍 Analisis
        btnAnalisis.setOnClickListener {
            if (imgPreview.drawable != null) {
                val bitmap = (imgPreview.drawable as? BitmapDrawable)?.bitmap

                if (bitmap != null) {
                    // Pre-process bitmap: YOLOv8 usually works best on square images.
                    // However, we pass the original bitmap and let YoloDetector handle resizing.
                    val results = yoloDetector.detect(bitmap)
                    
                    if (results.isNotEmpty()) {
                        val topResult = results.maxByOrNull { it.score }!!
                        
                        // Get disease info from DiseaseData
                        val info = DiseaseData.getInfo(topResult.label.replace("_", " "))
                        
                        val analysisResult = AnalysisResult(
                            diseaseName = topResult.label,
                            accuracy = topResult.score,
                            date = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
                            symptoms = info.first,
                            cause = info.second,
                            treatment = info.third,
                            imageBitmap = bitmap,
                            boundingBox = topResult.boundingBox
                        )
                        
                        AnalysisDataHolder.imageBitmap = bitmap
                        AnalysisDataHolder.isHistory = false
                        AnalysisDataHolder.analysisResult = analysisResult

                        val intent = Intent(requireContext(), DetailAnalisisActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "Tidak ada objek terdeteksi", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Ambil gambar dulu!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // 🔥 MODE: IMAGE
    private fun showImagePreview() {
        imgPreview.visibility = View.VISIBLE
        cameraPreview.visibility = View.GONE
        isRealtimeActive = false
    }

    // 🔥 START REALTIME
    private fun startRealtime() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 200)
            return
        }

        imgPreview.visibility = View.GONE
        cameraPreview.visibility = View.VISIBLE
        isRealtimeActive = true

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(cameraPreview.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview
            )

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // 🔥 STOP REALTIME
    private fun stopRealtime() {
        cameraPreview.visibility = View.GONE
        isRealtimeActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        yoloDetector.close()
    }
}