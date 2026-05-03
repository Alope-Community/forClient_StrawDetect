package com.mutia.deteksistrawberry

// ✅ WAJIB ADA
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import androidx.appcompat.app.AlertDialog

class HistoryAdapter(
    private val list: List<HistoryModel>,
    private val onDeleteClick: (HistoryModel) -> Unit,
    private val onItemClick: (HistoryModel) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtPenyakit: TextView = itemView.findViewById(R.id.txtPenyakit)
        val txtTanggal: TextView = itemView.findViewById(R.id.txtTanggal)
        val txtConfidence: TextView = itemView.findViewById(R.id.txtConfidence)
        val imgDaun: ImageView = itemView.findViewById(R.id.imgDaun)
        val btnDelete: View = itemView.findViewById(R.id.btnDeleteHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.txtPenyakit.text = data.namaPenyakit
        holder.txtTanggal.text = data.tanggal
        holder.txtConfidence.text = "${(data.confidence * 100).toInt()}%"

        val file = File(data.imageUrl)
        if (file.exists()) {
            holder.imgDaun.setImageURI(Uri.fromFile(file))
        }

        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Hapus Riwayat")
                .setMessage("Apakah Anda yakin ingin menghapus riwayat ini?")
                .setPositiveButton("Hapus", { _, _ ->
                    onDeleteClick(data)
                })
                .setNegativeButton("Tidak", null)
                .show()
        }

        holder.itemView.setOnClickListener {
            onItemClick(data)
        }
    }

    override fun getItemCount(): Int = list.size
}