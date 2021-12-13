package com.torrydo.hoyomi.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.torrydo.hoyomi.activities.pictureActivity
import com.torrydo.hoyomi.activities.playlistActivity
import com.torrydo.hoyomi.databinding.ItemPictureBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth

class playlist_recyclerAdapter(
    val context: Context,
    val mArrs: ArrayList<staggredFeedItems>,
    val screenWidth: Int,
    val screenHeight: Int,
    val mInterface:clickerInterface
) :
    RecyclerView.Adapter<playlist_recyclerAdapter.playlistHolder>() {

    class playlistHolder(val binding: ItemPictureBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): playlistHolder {
        val bind = ItemPictureBinding.inflate(LayoutInflater.from(context), parent, false)
        return playlistHolder(bind)
    }

    override fun onBindViewHolder(holder: playlistHolder, position: Int) {

        holder.binding.itemRecyclerImageView.also {

            val itemWidth = (screenWidth / 2).toInt() - Utils.getMyPx(20).toInt()

            var mRatio = 0.0

            if (mArrs[position].image.resolutions != null) {
                val tempx = mArrs[position].image.resolutions!![2]

                val pictureWidth = tempx.width
                mRatio = Math.round(
                    itemWidth.toDouble() / pictureWidth.toDouble()
                            * 10.0
                ) / 10.0

                it.requestLayout()
                it.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                it.layoutParams.height = (tempx.height * mRatio).toInt()

                Glide.with(context).load(convertSmth.replaceAmp(tempx.url))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.binding.itemRecyclerImageView)
            } else {
                val tempx = mArrs[position].image.source

                val pictureWidth = tempx.width
                mRatio = Math.round(
                    itemWidth.toDouble() / pictureWidth.toDouble()
                            * 10.0
                ) / 10.0

                it.requestLayout()
                it.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                it.layoutParams.height = (tempx.height * mRatio).toInt()

                Glide.with(context).load(convertSmth.HightReso_to_LowReso(tempx.url))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.binding.itemRecyclerImageView)
            }

        }

        holder.binding.itemRecycler3dots.setOnClickListener {
            val pop = PopupMenu(context,holder.binding.itemRecycler3dots).also {
                val v = it.menu.add("Delete picture")
                v.setOnMenuItemClickListener {
                    mInterface.click(position)
                    return@setOnMenuItemClickListener true
                }
            }
            pop.show()
        }

        holder.binding.itemRecyclerImageView.setOnClickListener {
            Intent(context as playlistActivity, pictureActivity::class.java).also {
                it.putExtra("wallpaper", Utils.getGsonInstance().toJson(mArrs[position]))
//                it.putExtra(CONSTANT.FROM_FORBOYS, CONSTANT.FROM_FORBOYS)

                /** check điều kiện reso null */
//                val options = ActivityOptionsCompat
//                    .makeSceneTransitionAnimation(
//                        context as playlistActivity,
//                        holder.binding.itemHomeConstraintLayout,
//                        ViewCompat.getTransitionName(holder.binding.itemHomeConstraintLayout)!!
//                        /** check điều kiện reso null */
//                    )
                context.startActivity(it/*, options.toBundle()*/)
            }
        }

    }

    override fun getItemCount(): Int {
        return mArrs.size
    }
}