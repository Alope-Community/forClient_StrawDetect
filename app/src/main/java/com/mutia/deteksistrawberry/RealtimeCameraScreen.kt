package com.mutia.deteksistrawberry

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

@Composable
fun RealtimeCameraScreen(onBack: () -> Unit) {
    var result by remember { mutableStateOf<AnalysisResult?>(null) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔥 Kamera
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    startCamera(ctx, this, executor)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 🔥 Overlay Bounding Box
        result?.boundingBox?.let { box ->
            BoundingBoxOverlay(box)
        }

        // Info Hasil di Layar
        result?.let {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = it.diseaseName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Akurasi: ${(it.accuracy * 100).toInt()}%",
                    color = Color.Green,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }

        // 🔙 Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

fun startCamera(
    context: Context,
    previewView: PreviewView,
    executor: java.util.concurrent.ExecutorService,
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            // Gunakan try-catch karena toBitmap() mungkin throw atau tidak ada tergantung versi
            try {
                val bitmap = imageProxy.toBitmap()
                // Rotasi bitmap sesuai sensor kamera
                val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                imageProxy.close()
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }, ContextCompat.getMainExecutor(context))
}

@Composable
fun BoundingBoxOverlay(rect: RectF) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val left = rect.left * size.width
        val top = rect.top * size.height
        val right = rect.right * size.width
        val bottom = rect.bottom * size.height

        drawRect(
            color = Color.Red,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
