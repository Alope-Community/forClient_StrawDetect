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
    private lateinit var cnnDetector: CNNDetector

    // Flag untuk mengecek apakah gambar sudah diupload/diambil
    private var isImageLoaded = false

    // 📷 Launcher untuk Kamera
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    showImagePreview()
                    imgPreview.setImageBitmap(bitmap)
                    isImageLoaded = true // Tandai gambar sudah ada
                }
            }
        }

    // 🖼️ Launcher untuk Galeri
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                showImagePreview()
                imgPreview.setImageURI(uri)
                isImageLoaded = true // Tandai gambar sudah ada
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inisialisasi View
        imgPreview = view.findViewById(R.id.imgPreview)
        cameraPreview = view.findViewById(R.id.cameraPreview)
        cnnDetector = CNNDetector(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()

        val btnCamera = view.findViewById<MaterialButton>(R.id.btnCamera)
        val btnGallery = view.findViewById<MaterialButton>(R.id.btnGallery)
        val btnRealtime = view.findViewById<MaterialButton>(R.id.btnRealtime)
        val btnAnalisis = view.findViewById<MaterialButton>(R.id.btnAnalisis)

        // Klik Tombol Kamera
        btnCamera.setOnClickListener {
            stopRealtime()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

        // Klik Tombol Galeri
        btnGallery.setOnClickListener {
            stopRealtime()
            galleryLauncher.launch("image/*")
        }

        // Klik Tombol Realtime (Pindah Activity)
        btnRealtime.setOnClickListener {
            val intent = Intent(requireContext(), RealtimeCameraActivity::class.java)
            startActivity(intent)
        }

        // Klik Tombol Analisis
        btnAnalisis.setOnClickListener {
            // Validasi: Jika flag isImageLoaded masih false, munculkan Toast
            if (!isImageLoaded) {
                Toast.makeText(requireContext(), "Silakan ambil atau upload gambar strawberry terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                performAnalysis()
            }
        }

        return view
    }

    private fun performAnalysis() {
        val drawable = imgPreview.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val results = cnnDetector.detect(bitmap)

            if (results.isNotEmpty()) {
                val topResult = results.maxByOrNull { it.score }!!

                // Ambil data penyakit berdasarkan label
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

                // Simpan ke Singleton Data Holder
                AnalysisDataHolder.imageBitmap = bitmap
                AnalysisDataHolder.isHistory = false
                AnalysisDataHolder.analysisResult = analysisResult

                // Pindah ke halaman detail
                val intent = Intent(requireContext(), DetailAnalisisActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Objek tidak dikenali. Pastikan gambar strawberry jelas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImagePreview() {
        imgPreview.visibility = View.VISIBLE
        cameraPreview.visibility = View.GONE
    }

    private fun stopRealtime() {
        cameraPreview.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cnnDetector.close()
    }
}