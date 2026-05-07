package com.mutia.deteksistrawberry

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

@Composable
fun RealtimeCameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var result by remember { mutableStateOf<AnalysisResult?>(null) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val yoloDetector = remember { YoloDetector(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraProvider?.unbindAll()
            executor.shutdownNow()
            yoloDetector.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔥 Kamera
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(this.surfaceProvider)

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setTargetRotation(this.display.rotation)
                            .build()

                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            try {
                                if (executor.isShutdown) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }
                                
                                val bitmap = imageProxy.toBitmap()
                                val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
                                
                                val results = yoloDetector.detect(rotatedBitmap)
                                if (results.isNotEmpty()) {
                                    val topResult = results.maxByOrNull { it.score }!!
                                    val info = DiseaseData.getInfo(topResult.label.replace("_", " "))
                                    
                                    val analysisResult = AnalysisResult(
                                        diseaseName = topResult.label,
                                        accuracy = topResult.score,
                                        date = "",
                                        symptoms = info.first,
                                        cause = info.second,
                                        treatment = info.third,
                                        imageBitmap = rotatedBitmap,
                                        boundingBox = topResult.boundingBox
                                    )
                                    result = analysisResult
                                } else {
                                    result = null
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 🔥 Overlay Bounding Box
        result?.let { res ->
            res.boundingBox?.let { box ->
                res.imageBitmap?.let { bitmap ->
                    BoundingBoxOverlay(box, res.diseaseName, res.accuracy, bitmap.width, bitmap.height)
                }
            }
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

@Composable
fun BoundingBoxOverlay(rect: RectF, label: String, confidence: Float, imgWidth: Int, imgHeight: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val containerWidth = size.width
        val containerHeight = size.height
        
        val scaleX = containerWidth / imgWidth
        val scaleY = containerHeight / imgHeight
        // Use minOf for "Fit" or just direct mapping if PreviewView is fillMaxSize
        val scale = minOf(scaleX, scaleY)
        
        val drawWidth = imgWidth * scale
        val drawHeight = imgHeight * scale
        
        val offsetX = (containerWidth - drawWidth) / 2
        val offsetY = (containerHeight - drawHeight) / 2

        val left = offsetX + rect.left * drawWidth
        val top = offsetY + rect.top * drawHeight
        val right = offsetX + rect.right * drawWidth
        val bottom = offsetY + rect.bottom * drawHeight

        // Draw Bounding Box
        drawRect(
            color = Color.Red,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw Label Background
        val text = "$label ${(confidence * 100).toInt()}%"
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = Color.Red.toArgb()
                textSize = 40f
                typeface = Typeface.DEFAULT_BOLD
            }
            val textWidth = paint.measureText(text)
            val textHeight = 45f

            // Draw background for text
            drawRect(
                color = Color.Red,
                topLeft = Offset(left, maxOf(0f, top - textHeight)),
                size = androidx.compose.ui.geometry.Size(textWidth + 20f, textHeight)
            )

            // Draw text
            drawText(
                text,
                left + 10f,
                maxOf(textHeight - 10f, top - 10f),
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                    typeface = Typeface.DEFAULT_BOLD
                }
            )
        }
    }
}

fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
