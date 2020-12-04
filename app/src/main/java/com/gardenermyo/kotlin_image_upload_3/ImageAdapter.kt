package com.gardenermyo.kotlin_image_upload_3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_image.view.*

class ImageAdapter( val urls: List<String>): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_image,parent,false))
    }

    override fun getItemCount(): Int {
        return urls.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        var url = urls[position]
        Glide.with(holder.itemView).load(url).into(holder.itemView.item_image_view)

//        Picasso.get()
//            .load(url)
//            .placeholder(R.drawable.ic_launcher_background)
//            .into(holder.itemView.item_image_view)
    }

}