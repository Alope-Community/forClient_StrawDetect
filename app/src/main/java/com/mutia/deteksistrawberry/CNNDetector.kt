package com.mutia.deteksistrawberry

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod

class CNNDetector(private val context: Context) {

    private var interpreter: Interpreter? = null

    // LABEL
    private val labels = listOf(
        "healthy",
        "leaf_blight",
        "leaf_scorch",
        "leaf_spot"
    )

    // SESUAI TRAINING
    private val inputSize = 224

    init {
        val model = FileUtil.loadMappedFile(
            context,
            "strawberry_detector.tflite"
        )

        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)

        // DEBUG SHAPE
        val inputShape = interpreter!!.getInputTensor(0).shape()
        val outputShape = interpreter!!.getOutputTensor(0).shape()

        Log.d("CNNDetector", "Input Shape: ${inputShape.contentToString()}")
        Log.d("CNNDetector", "Output Shape: ${outputShape.contentToString()}")
    }

    @Synchronized
    fun detect(bitmap: Bitmap): List<Detection> {

        val currentInterpreter = interpreter ?: return emptyList()

        // =========================
        // PREPROCESSING
        // =========================

        val imageProcessor = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    inputSize,
                    inputSize,
                    ResizeMethod.BILINEAR
                )
            )

            // SESUAI TRAINING: img / 255.0
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)

        tensorImage.load(bitmap)

        tensorImage = imageProcessor.process(tensorImage)

        // =========================
        // OUTPUT MODEL
        // =========================
        // MODEL:
        //
        // [xmin, ymin, xmax, ymax,
        //  c1, c2, c3, c4]
        //
        // TOTAL = 8
        // =========================

        val output = Array(1) { FloatArray(8) }

        currentInterpreter.run(
            tensorImage.buffer,
            output
        )

        val result = output[0]

        Log.d("CNNDetector", "Raw Output: ${result.contentToString()}")

        // =========================
        // BOUNDING BOX
        // =========================

        val xmin = result[0].coerceIn(0f, 1f)
        val ymin = result[1].coerceIn(0f, 1f)
        val xmax = result[2].coerceIn(0f, 1f)
        val ymax = result[3].coerceIn(0f, 1f)

        // =========================
        // CLASS SCORES
        // =========================

        val scores = result.sliceArray(4..7)

        var maxScore = scores[0]
        var classId = 0

        for (i in scores.indices) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                classId = i
            }
        }

        Log.d("CNNDetector", "Class ID: $classId")
        Log.d("CNNDetector", "Confidence: $maxScore")

        // =========================
        // CEK CONFIDENCE
        // =========================

        val finalLabel =
            if (maxScore < 0.9f) {
                "Bukan Daun Strawberry"
            } else {
                labels[classId]
            }

        val detection = Detection(
            boundingBox = RectF(
                xmin,
                ymin,
                xmax,
                ymax
            ),

            label = finalLabel,

            score = maxScore
        )

        return listOf(detection)
    }

    @Synchronized
    fun close() {
        interpreter?.close()
        interpreter = null
    }

    data class Detection(
        val boundingBox: RectF,
        val label: String,
        val score: Float
    )
}