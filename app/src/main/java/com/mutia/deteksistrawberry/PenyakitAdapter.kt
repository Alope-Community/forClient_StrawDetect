package com.mutia.deteksistrawberry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class PenyakitAdapter(private val list: List<Penyakit>) :
    RecyclerView.Adapter<PenyakitAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img)
        val tvJudul: TextView = itemView.findViewById(R.id.tvJudul)
        val tvDeskripsi: TextView = itemView.findViewById(R.id.tvDeskripsi)
        val tvToggle: TextView = itemView.findViewById(R.id.tvToggle)
        val layoutDetail: LinearLayout = itemView.findViewById(R.id.layoutDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_penyakit, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // SET DATA
        holder.tvJudul.text = item.nama
        holder.tvDeskripsi.text = item.deskripsi
        holder.img.setImageResource(item.gambar)

        // STATE EXPAND / COLLAPSE
        if (item.isExpanded) {
            holder.layoutDetail.visibility = View.VISIBLE
            holder.tvToggle.text = "Tutup"
        } else {
            holder.layoutDetail.visibility = View.GONE
            holder.tvToggle.text = "Selengkapnya"
        }

        // CLICK BUTTON
        holder.tvToggle.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }
}