package com.mutia.deteksistrawberry

// ✅ WAJIB ADA
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val list: List<HistoryModel>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtPenyakit: TextView = itemView.findViewById(R.id.txtPenyakit)
        val txtTanggal: TextView = itemView.findViewById(R.id.txtTanggal)
        val txtConfidence: TextView = itemView.findViewById(R.id.txtConfidence)
        val imgDaun: ImageView = itemView.findViewById(R.id.imgDaun)
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
        holder.txtConfidence.text = "Akurasi: ${data.confidence}%"
    }

    override fun getItemCount(): Int = list.size
}