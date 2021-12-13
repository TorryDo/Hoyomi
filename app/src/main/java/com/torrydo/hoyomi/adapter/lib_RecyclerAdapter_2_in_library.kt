package com.torrydo.hoyomi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.torrydo.hoyomi.databinding.ItemLibRecyclerview2InLibraryBinding

class lib_RecyclerAdapter_2_in_library(_context: Context,_mArr:ArrayList<Int>) :
    RecyclerView.Adapter<lib_RecyclerAdapter_2_in_library.libHolder>() {

    val context = _context
    val mArr = _mArr

    class libHolder(val binding: ItemLibRecyclerview2InLibraryBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): libHolder {
        val bind = ItemLibRecyclerview2InLibraryBinding.inflate(LayoutInflater.from(context),parent,false)
        return libHolder(bind)
    }

    override fun onBindViewHolder(holder: libHolder, position: Int) {

        Glide.with(context)
            .load(mArr[position])
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.binding.itemLib2ImageView)
    }

    override fun getItemCount(): Int {
        return mArr.size
    }
}