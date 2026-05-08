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
    private var labels = listOf<String>()

    private var inputWidth = 640
    private var inputHeight = 640
    private var numOutput = 8400
    private var numClasses = 0
    private var isClassification = false

    init {
        val model = FileUtil.loadMappedFile(context, "strawberry_detecor.tflite")
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)

        val inputShape = interpreter!!.getInputTensor(0).shape()
        // inputShape is usually [1, 640, 640, 3] or [640, 640, 3]
        if (inputShape.size == 4) {
            inputHeight = inputShape[1]
            inputWidth = inputShape[2]
        } else {
            inputHeight = inputShape[0]
            inputWidth = inputShape[1]
        }

        val outputShape = interpreter!!.getOutputTensor(0).shape()
        Log.d("CNNDetector", "Output Shape: ${outputShape.contentToString()}")
        
        val loadedLabels = FileUtil.loadLabels(context, "labels.txt")
        Log.d("CNNDetector", "Loaded Labels: $loadedLabels")

        val labelCount = loadedLabels.size
        
        if (outputShape.size == 3) {
            val d1 = outputShape[1]
            val d2 = outputShape[2]

            if (d1 < d2) {
                numClasses = if (d1 > 4) d1 - 4 else d1
                numOutput = d2
            } else {
                numClasses = if (d2 > 5) d2 - 5 else d2
                numOutput = d1
            }
        } else if (outputShape.size == 2) {
            if (outputShape[0] == 1) {
                // [1, C] -> Classification
                isClassification = true
                numClasses = outputShape[1]
                numOutput = 1
            } else {
                // [boxes, 4+C] or [boxes, 5+C]
                numOutput = outputShape[0]
                numClasses = if (outputShape[1] > 5) outputShape[1] - 5 else if (outputShape[1] > 4) outputShape[1] - 4 else outputShape[1]
            }
        } else {
            numClasses = labelCount
            numOutput = 1
        }

        labels = loadedLabels
        // If the model has more classes than the label file, we'll map them later
        Log.d("CNNDetector", "Final numClasses: $numClasses, numOutput: $numOutput, isClassification: $isClassification")
    }

    @Synchronized
    fun detect(bitmap: Bitmap): List<Detection> {
        val currentInterpreter = interpreter ?: return emptyList()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputHeight, inputWidth, ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val outputShape = currentInterpreter.getOutputTensor(0).shape()
        val finalOutput: Array<FloatArray>

        if (outputShape.size == 3) {
            val output = Array(1) { Array(outputShape[1]) { FloatArray(outputShape[2]) } }
            currentInterpreter.run(tensorImage.buffer, output)
            finalOutput = output[0]
        } else {
            finalOutput = Array(outputShape[0]) { FloatArray(outputShape[1]) }
            currentInterpreter.run(tensorImage.buffer, finalOutput)
        }

        return postProcess(finalOutput)
    }

    private fun postProcess(output: Array<FloatArray>): List<Detection> {
        val detections = mutableListOf<Detection>()
        
        if (isClassification) {
            val scores = output[0]
            var maxScore = 0f
            var classId = -1
            for (i in 0 until numClasses) {
                if (i < scores.size && scores[i] > maxScore) {
                    maxScore = scores[i]
                    classId = i
                }
            }
            if (classId != -1 && maxScore > 0.4f) {
                val labelName = when {
                    classId < labels.size -> labels[classId]
                    classId == 4 -> "Leaf Spot" // User identified ID 4 as Leaf Spot
                    classId == 5 -> "Leaf Blight" // User identified ID 5 as Leaf Blight
                    else -> "Penyakit Lain"
                }
                detections.add(
                    Detection(
                        RectF(0f, 0f, 1f, 1f),
                        labelName,
                        maxScore
                    )
                )
            }
            return detections
        }

        // output format: [4 + numClasses, boxes] or similar
        for (i in 0 until numOutput) {
            var maxScore = 0f
            var classId = -1
            
            // Offset for score calculation: usually 4 (x,y,w,h) or 5 (x,y,w,h,conf)
            val scoreOffset = if (output.size > numClasses + 4) 5 else 4

            for (c in 0 until numClasses) {
                val scoreIndex = c + scoreOffset
                if (scoreIndex < output.size && output[scoreIndex][i] > maxScore) {
                    maxScore = output[scoreIndex][i]
                    classId = c
                }
            }

            if (maxScore > 0.4f && classId != -1) {
                var cx = output[0][i]
                var cy = output[1][i]
                var w = output[2][i]
                var h = output[3][i]

                // Check if coordinates are normalized (0-1) or absolute (0-640)
                // Use a safer threshold for absolute coordinates
                if (cx > 2.0f || cy > 2.0f || w > 2.0f || h > 2.0f) {
                    cx /= inputWidth
                    cy /= inputHeight
                    w /= inputWidth
                    h /= inputHeight
                }

                val x1 = maxOf(0f, cx - w / 2f)
                val y1 = maxOf(0f, cy - h / 2f)
                val x2 = minOf(1f, cx + w / 2f)
                val y2 = minOf(1f, cy + h / 2f)

                val labelName = when {
                    classId < labels.size -> labels[classId]
                    classId == 4 -> "Leaf Spot" // User identified ID 4 as Leaf Spot
                    classId == 5 -> "Leaf Blight" // User identified ID 5 as Leaf Blight
                    else -> "Penyakit Lain"
                }
                detections.add(
                    Detection(
                        RectF(x1, y1, x2, y2),
                        labelName,
                        maxScore
                    )
                )
            }
        }

        return nms(detections)
    }

    private fun nms(detections: List<Detection>): List<Detection> {
        Log.d("CNNDetector", "NMS Input: ${detections.size}")
        val sortedDetections = detections.sortedByDescending { it.score }
        val selectedDetections = mutableListOf<Detection>()
        val active = BooleanArray(sortedDetections.size) { true }

        for (i in sortedDetections.indices) {
            if (active[i]) {
                selectedDetections.add(sortedDetections[i])
                for (j in i + 1 until sortedDetections.size) {
                    if (active[j] && iou(sortedDetections[i].boundingBox, sortedDetections[j].boundingBox) > 0.45f) {
                        active[j] = false
                    }
                }
            }
        }
        Log.d("CNNDetector", "NMS Output: ${selectedDetections.size}")
        return selectedDetections
    }

    private fun iou(box1: RectF, box2: RectF): Float {
        val intersectionLeft = maxOf(box1.left, box2.left)
        val intersectionTop = maxOf(box1.top, box2.top)
        val intersectionRight = minOf(box1.right, box2.right)
        val intersectionBottom = minOf(box1.bottom, box2.bottom)

        val intersectionArea = maxOf(0f, intersectionRight - intersectionLeft) * maxOf(0f, intersectionBottom - intersectionTop)
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)

        return intersectionArea / (box1Area + box2Area - intersectionArea)
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
