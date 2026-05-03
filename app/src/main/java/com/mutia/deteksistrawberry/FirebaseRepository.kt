package com.mutia.deteksistrawberry

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveHistory(userId: String, history: HistoryModel) {
        db.collection("users")
            .document(userId)
            .collection("history")
            .add(history)
            .addOnSuccessListener {
                Log.d("FIREBASE", "History berhasil disimpan")
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "History gagal disimpan: ${it.message}")
            }
    }

    fun deleteHistoryWithLocalImage(
        userId: String,
        documentId: String,
        imagePath: String?,
        onComplete: (Boolean) -> Unit
    ) {
        // 1. Hapus file lokal jika ada
        if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d("LOCAL", "Gambar lokal berhasil dihapus")
                } else {
                    Log.e("LOCAL", "Gagal hapus gambar lokal")
                }
            }
        }

        // 2. Hapus data dari Firestore
        db.collection("users")
            .document(userId)
            .collection("history")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.d("FIREBASE", "History berhasil dihapus")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "Gagal hapus history: ${it.message}")
                onComplete(false)
            }
    }

    fun uploadImage(bitmap: Bitmap, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference

        val fileName = "images/${System.currentTimeMillis()}.jpg"
        val fileRef = storageRef.child(fileName)

        val byteArray = bitmapToByteArray(bitmap)

        fileRef.putBytes(byteArray)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "Upload gagal: ${it.message}")
            }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap): String {
        val directory = File(context.filesDir, "images")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)

        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        stream.flush()
        stream.close()

        return file.absolutePath
    }

    fun getHistory(userId: String, onResult: (List<HistoryModel>) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("history")
            .orderBy("tanggal")
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(HistoryModel::class.java)
                onResult(list)
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "Gagal ambil data: ${it.message}")
            }
    }
}