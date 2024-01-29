package com.pnpawd.userapp.adapters


import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.models.LandRecordsModel
import com.pnpawd.userapp.R

class ImageRecyclerView(private var imageModel: List<LandRecordsModel>) : RecyclerView.Adapter<ImageRecyclerView.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var img: ImageView = itemView.findViewById(R.id.image_data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_imageview, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: LandRecordsModel = imageModel[position]
        val imgBitmap = BitmapFactory.decodeFile(model.image)
        holder.img.setImageBitmap(imgBitmap)
        holder.img.rotation = model.rotate.toFloat()
    }


    override fun getItemCount(): Int {
        return imageModel.size
    }

}