package com.pnpawd.userapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.ImageModel

internal class Image_Recyclerview(private var imageModel: List<ImageModel>) : RecyclerView.Adapter<Image_Recyclerview.MyViewHolder>() {

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.image_recyclerview, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val Image = imageModel[position]
        holder.image.setImageBitmap(Image.getImage())
        holder.image.rotation = Image.getRotate().toFloat()
    }

    override fun getItemCount(): Int {
        return imageModel.size
    }
}