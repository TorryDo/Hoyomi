package com.torrydo.hoyomi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.databinding.ItemPlaylistBinding
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.interfaces.RecyclerInterface
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth

class adapter_autoWallpaper(_context: Context, _mArrs:ArrayList<Playlist>, _interface : RecyclerInterface)
    : RecyclerView.Adapter<adapter_autoWallpaper.pagerHolder>() {

    private val context = _context
    private var mArrs = _mArrs
    private val mInterface = _interface

    class pagerHolder(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): pagerHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(context),parent,false)
        return pagerHolder(binding)
    }

    override fun onBindViewHolder(holder: pagerHolder, position: Int) {
        var slink = CONSTANT.DEFAULT_IMG
        if (mArrs[position].list?.isEmpty() == false) {

                if (mArrs[position].list!![0].image.resolutions != null) {
                    slink = mArrs[position].list!![0].image.resolutions!![1].url.replace("amp;","")
                } else {
                    slink = convertSmth.HightReso_to_LowReso(mArrs[position].list!![0].image.source.url)
                }
                holder.binding.itemLibTxtTitle.text = mArrs[position].playlistTitle.toString()
            }


        holder.binding.constraintLayout.also {
            it.requestLayout()
            it.layoutParams.height = Utils.getMyPx(180)
        }

        holder.binding.itemLibImageView.also {

            Glide.with(context).load(slink)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(it)
        }

        holder.binding.itemLibImageView.setOnClickListener {
            mInterface.onItemCLick(position,holder.binding)
        }
        holder.binding.itemLibImageView.setOnLongClickListener {
            mInterface.onItemLongCLick(position,holder.binding)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return mArrs.size
    }
}