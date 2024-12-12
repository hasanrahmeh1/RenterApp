package com.example.renterapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.renterapp.databinding.ItemPropertyBinding

class WatchlistAdapter(private var properties: MutableList<Property>) :
    RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    fun updateList(newProperties: List<Property>) {
        properties.clear()
        properties.addAll(newProperties)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val binding =
            ItemPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WatchlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        holder.bind(properties[position])
    }

    override fun getItemCount(): Int = properties.size



    inner class WatchlistViewHolder(private val binding: ItemPropertyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(property: Property) {
            with(binding) {
                propertyAddress.text = property.address
                propertyDetails.text =
                    "Price: $${property.monthlyRentalPrice}\nBedrooms: ${property.numberOfBedrooms}"
                Glide.with(propertyImage.context).load(property.imageUrl).into(propertyImage)

                removeButton.setOnClickListener {
                    properties.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                }
            }
        }
    }
}
