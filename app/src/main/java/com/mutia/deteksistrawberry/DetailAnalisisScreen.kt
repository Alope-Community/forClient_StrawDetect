package com.mutia.deteksistrawberry

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.tooling.preview.Preview

data class AnalysisResult(
    val diseaseName: String,
    val accuracy: Float,
    val date: String,
    val symptoms: String,
    val cause: String,
    val treatment: String,
    val imageBitmap: Bitmap?,
    val boundingBox: RectF?
)

//@Preview(showBackground = true)
//@Composable
//fun PreviewDetailAnalisis() {
//    MaterialTheme {
//        DetailAnalisisScreen(
//            result = AnalysisResult(
//                diseaseName = "Leaf Scorch",
//                accuracy = 0.92f,
//                date = "12 Oktober 2023",
//                symptoms = "Bercak coklat kemerahan pada tepi daun yang perlahan mengering.",
//                cause = "Jamur Diplocarpon earliana.",
//                treatment = "Cabut daun yang terinfeksi dan gunakan fungisida secara berkala.",
//                imageBitmap = null,
//                boundingBox = RectF(0.2f, 0.2f, 0.8f, 0.6f)
//            )
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAnalisisScreen(
    result: AnalysisResult,
    isHistory: Boolean,
    onBackClick: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Detail Analisis",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD81B60)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFFDE7ED)) // Light pink background
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gambar Hasil
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (result.imageBitmap != null) {
                        Image(
                            bitmap = result.imageBitmap.asImageBitmap(),
                            contentDescription = "Gambar Deteksi",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // bounding box
                        result.boundingBox?.let { rect ->
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val left = rect.left * size.width
                                val top = rect.top * size.height
                                val right = rect.right * size.width
                                val bottom = rect.bottom * size.height

                                drawRect(
                                    color = Color.Red,
                                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(
                                        right - left,
                                        bottom - top
                                    ),
                                    style = Stroke(width = 4.dp.toPx())
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nama Penyakit & Confidence
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hasil Deteksi",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            Text(
                                text = result.diseaseName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD81B60)
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { result.accuracy },
                                modifier = Modifier.size(60.dp),
                                color = Color(0xFFD81B60),
                                strokeWidth = 6.dp,
                                trackColor = Color(0xFFFDE7ED),
                            )
                            Text(
                                text = "${(result.accuracy * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD81B60)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFFDE7ED))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Deteksi pada: ${result.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Detail
            InfoSection(
                title = "Gejala",
                content = result.symptoms,
                icon = Icons.Default.Search,
                accentColor = Color(0xFFD81B60)
            )
            InfoSection(
                title = "Penyebab",
                content = result.cause,
                icon = Icons.Default.BugReport,
                accentColor = Color(0xFFE91E63)
            )
            InfoSection(
                title = "Penanganan",
                content = result.treatment,
                icon = Icons.Default.MedicalServices,
                accentColor = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                enabled = !isHistory,
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD81B60)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Selesai", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoSection(title: String, content: String, icon: ImageVector, accentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp),
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = Color.DarkGray
            )
        }
    }
}
