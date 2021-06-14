package com.example.sergeyportfolioapp.shibaphotodisplay.ui.main

import android.content.Context
import android.graphics.Movie
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sergeyportfolioapp.R
import com.google.android.material.card.MaterialCardView


class RecyclerViewAdapter internal constructor(private val shibaImages: List<String>,
                                               private val context : Context) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.shiba_recycler_view_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val image = shibaImages[position]
        Glide
            .with(context)
            .load(image)
            .into(holder.image);
    }

    override fun getItemCount(): Int {
        return shibaImages.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val image: ImageView
         val cardView: MaterialCardView

        init {
            image = itemView.findViewById(R.id.imageView)
            cardView = itemView.findViewById(R.id.card_view)
        }
    }
}