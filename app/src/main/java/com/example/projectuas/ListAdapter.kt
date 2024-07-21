package com.example.projectuas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(private val lokasilist: List<ListHead>) : RecyclerView.Adapter<ListAdapter.Listviewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Listviewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,
            parent, false)
        return Listviewholder(itemView)
    }

    override fun onBindViewHolder(holder: Listviewholder, position: Int) {
        val currentItem = lokasilist[position]
        holder.judul.text = currentItem.judul
        holder.lokasi.text = currentItem.lokasi

    }

    override fun getItemCount() = lokasilist.size

    class Listviewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul: TextView = itemView.findViewById(R.id.judul)
        val lokasi: TextView = itemView.findViewById(R.id.lokasi)
    }
}
