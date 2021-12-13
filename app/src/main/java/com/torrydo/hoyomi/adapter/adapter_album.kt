package com.torrydo.hoyomi.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.activities.playlistActivity
import com.torrydo.hoyomi.databinding.ItemPlaylistBinding
import com.torrydo.hoyomi.interfaces.playlist_interface
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.utility.convertSmth

class adapter_album(
    _context: Context,
    _mArr: ArrayList<Playlist>,
    _reso: Int,
    _deviceWidth: Int,
    _mInterface: playlist_interface
) :
    RecyclerView.Adapter<adapter_album.libViewHolder>() {

    val context = _context
    val mArr = _mArr
    val resoNum = _reso
    val deviceWidth = _deviceWidth
    val mInterface = _mInterface

    class libViewHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): libViewHolder {
        val bind = ItemPlaylistBinding.inflate(LayoutInflater.from(context), parent, false)
        return libViewHolder(bind)
    }

    override fun onBindViewHolder(holder: libViewHolder, position: Int) {
        var tempStr = CONSTANT.DEFAULT_IMG

//        if (mArr[position].list?.isEmpty() == false) {
            if (resoNum == 1) {
                if (mArr[position].list?.isEmpty() == false) {

                    if (mArr[position].list!![0].image.resolutions != null) {
                        tempStr =
                            mArr[position].list!![0].image.resolutions!![resoNum].url.toString()
                    } else {
                        tempStr =
                            convertSmth.HightReso_to_LowReso(mArr[position].list!![0].image.source.url)
                    }
                    holder.binding.itemLibTxtTitle.text = mArr[position].playlistTitle.toString()
                }else{
                    holder.binding.itemLibTxtTitle.text = mArr[position].playlistTitle.toString()
                }
            } else if (resoNum == 2) {

//                Log.e("XXY", "device width = $deviceWidth")
                holder.binding.constraintLayout.also {
                    it.requestLayout()
                    it.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                    it.layoutParams.height = (deviceWidth.toLong() / 3 * 1.7).toInt()
                }
                if (mArr[position].list?.isEmpty() == false) {
                    if (mArr[position].list!![0].image.resolutions != null) {

                        tempStr =
                            mArr[position].list!![0].image.resolutions!![resoNum].url.toString()
                        holder.binding.itemLibTxtTitle.text =
                            mArr[position].playlistTitle.toString()

                    } else {
                        /** nếu reso = null thì lấy nguồn từ source */
                        tempStr =
                            convertSmth.HightReso_to_LowReso(mArr[position].list!![0].image.source.url)

                        holder.binding.itemLibTxtTitle.text =
                            mArr[position].playlistTitle.toString()
                    }
                }else{
                    holder.binding.itemLibTxtTitle.text =
                        mArr[position].playlistTitle.toString()
                }
            }

        val newStr = convertSmth.replaceAmp(tempStr)

        Glide.with(context).load(newStr)
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.binding.itemLibImageView)

        holder.binding.itemLibImageView.setOnClickListener {
//            val tempJsonStr = Utils.fromPlaylist_to_String(mArr[position])
            val i = Intent(context, playlistActivity::class.java)

            i.putExtra("lib_RecyclerApapter_in_library", position)
            context.startActivity(i)
        }
        holder.binding.itemLibImageView.setOnLongClickListener {
            mInterface.onItemLongCLick(position)
            return@setOnLongClickListener true
        }

    }


    override fun getItemCount(): Int {
        return mArr.size
    }
}