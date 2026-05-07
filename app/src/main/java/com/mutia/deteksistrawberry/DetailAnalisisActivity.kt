package com.mutia.deteksistrawberry

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailAnalisisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = FirebaseRepository()

        // Ambil data dari holder (atau gunakan dummy jika kosong)
        val result = AnalysisDataHolder.analysisResult ?: AnalysisResult(
            diseaseName = "Unknown",
            accuracy = 0f,
            date = "-",
            symptoms = "-",
            cause = "-",
            treatment = "-",
            imageBitmap = AnalysisDataHolder.imageBitmap,
            boundingBox = null
        )

        setContent {
            MaterialTheme {
                DetailAnalisisScreen(
                    result = result,
                    isHistory = AnalysisDataHolder.isHistory,
                    onBackClick = { finish() },
                    onConfirm = {
                        val bitmap = result.imageBitmap

                        if (bitmap != null) {
                            val imagePath = repository.saveImageToInternalStorage(this, bitmap)

                            val history = HistoryModel(
                                namaPenyakit = result.diseaseName,
                                tanggal = SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm",
                                    Locale.getDefault()
                                ).format(Date()),
                                imageUrl = imagePath,
                                confidence = result.accuracy,
                                boxLeft = result.boundingBox?.left ?: 0f,
                                boxTop = result.boundingBox?.top ?: 0f,
                                boxRight = result.boundingBox?.right ?: 0f,
                                boxBottom = result.boundingBox?.bottom ?: 0f
                            )

                            repository.saveHistory("user_dummy", history)
                            finish()
                        }
                    }
                )
            }
        }
    }
}

object AnalysisDataHolder {
    var imageBitmap: Bitmap? = null
    var analysisResult: AnalysisResult? = null
    var isHistory: Boolean = false
}
