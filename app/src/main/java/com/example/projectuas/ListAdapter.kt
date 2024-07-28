package com.example.projectuas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(
    private val lokasilist: MutableList<ListHead>,
    private val onItemClick: (ListHead) -> Unit,
    private val onItemLongClick: (Int) -> Unit
) : RecyclerView.Adapter<ListAdapter.Listviewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Listviewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Listviewholder(itemView)
    }

    override fun onBindViewHolder(holder: Listviewholder, position: Int) {
        val currentItem = lokasilist[position]
        holder.nama_lokasi.text = currentItem.nama_lokasi
        holder.koordinat_lokasi.text = currentItem.koordinat_lokasi

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(position)
            true
        }
    }

    override fun getItemCount() = lokasilist.size

    class Listviewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama_lokasi: TextView = itemView.findViewById(R.id.nama_lokasi)
        val koordinat_lokasi: TextView = itemView.findViewById(R.id.koordinat_lokasi)
    }

    fun removeItem(position: Int) {
        lokasilist.removeAt(position)
        notifyItemRemoved(position)
    }
}
