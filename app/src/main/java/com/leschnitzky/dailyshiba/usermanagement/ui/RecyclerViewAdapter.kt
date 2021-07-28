package com.leschnitzky.dailyshiba.usermanagement.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.leschnitzky.dailyshiba.R
import com.google.android.material.card.MaterialCardView
import timber.log.Timber


class RecyclerViewAdapter internal constructor(
    private val shibaImages: List<String>,
    private val context: Context,
    private val viewModel: UserViewModel,
) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
    interface PhotoSelectedListener {
        fun onPhotoSelected(imageView: ImageView, uri: String, position: Int)
    }
    lateinit var photoSelectedListener: PhotoSelectedListener

    private val TAG = "RecyclerViewAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.shiba_recycler_view_list_item, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val image = shibaImages[position]
        holder.image.transitionName = image

        holder.image.setOnClickListener {
            photoSelectedListener.onPhotoSelected(holder.image, image, position)

        }
            Glide
                .with(context)
                .load(image)
                .override(250,250)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.image)
    }

    override fun getItemCount(): Int {
        return shibaImages.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val image: ImageView
         val cardView: MaterialCardView

        init {
            image = itemView.findViewById(R.id.drawer_profile_pic)
            cardView = itemView.findViewById(R.id.card_view)
        }
    }
}