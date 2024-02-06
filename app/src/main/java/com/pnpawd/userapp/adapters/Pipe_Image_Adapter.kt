package com.pnpawd.userapp.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.PipeImageModel


internal class Pipe_Image_Adapter(private var pipeImageModel: ArrayList<PipeImageModel>) : RecyclerView.Adapter<Pipe_Image_Adapter.MyViewHolder>() {

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        var remove: ImageView = view.findViewById(R.id.image_cancel)
        var image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.image_recyclerview, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = pipeImageModel[position]
        val imgBitmap = BitmapFactory.decodeFile(model.getImage())
        holder.image.setImageBitmap(imgBitmap)
        holder.image.scaleType = ImageView.ScaleType.FIT_XY
        holder.image.rotation = model.getRotate().toFloat()

//        holder.remove.setOnClickListener(View.OnClickListener {
//            pipeImageModel.removeAt(position)
//            notifyItemRemoved(position)
//            notifyItemRangeChanged(position, pipeImageModel.size)
//        })

    }

    override fun getItemCount(): Int {
        return  pipeImageModel.size
    }
}