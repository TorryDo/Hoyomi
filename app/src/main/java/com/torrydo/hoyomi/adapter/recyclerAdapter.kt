package com.torrydo.hoyomi.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.torrydo.hoyomi.*
import com.torrydo.hoyomi.databinding.ItemPictureBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.interfaces.longClickInterface
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth
import kotlin.random.Random

class recyclerAdapter(
    private val context: Context,
    private val mArr: ArrayList<staggredFeedItems>,
    private val screenWidth: Int,
    _height: Int,
    private val mInterface: clickerInterface,
    private val mLongClickInterface: longClickInterface
) : RecyclerView.Adapter<recyclerAdapter.viewHolder>() {


    class viewHolder(val binding: ItemPictureBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {

        val tempB = ItemPictureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return viewHolder(tempB)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
//        holder.setIsRecyclable(false)
        forBoys(holder, position)

    }


    private fun forBoys(holder: viewHolder, position: Int) {

        val tempx = mArr[position].image.resolutions?.get(CONSTANT.resolution_of_StaggredItem -1 )
        if (tempx != null) {
//            Log.e("_info", "tempx != null , $tempx ")

            val tempLink = convertSmth.replaceAmp(tempx!!.url)

            /** check điều kiện reso null */
            val pictureWidth = tempx.width
            val pictureHeight = tempx.height


            holder.binding.itemRecyclerImageView.also {
                val random: Int = Random.nextInt(1, 3)
                if (context != null) {
                    when (random) {
                        1 -> it.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.white_blue
                            )
                        )
                        2 -> it.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.white_pink
                            )
                        )
//                    3 -> it.setBackgroundColor(
//                        ContextCompat.getColor(
//                            context,
//                            R.color.white_orange
//                        )
//                    )
                    }
                }

                val itemWidth = (screenWidth / 2).toInt() - Utils.getMyPx(20).toInt()

                var mRatio = 0.0
                mRatio = Math.round(
                    itemWidth.toDouble() / pictureWidth.toDouble()
                            * 10.0
                ) / 10.0

//            Log.d("fck", "mratio = $mRatio , pictureWidth = $pictureWidth , itemWidth = $itemWidth")

                it.requestLayout()
                it.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                it.layoutParams.height = (pictureHeight * mRatio).toInt()

                /** check if image link available */
                if (context != null) {
                    Glide.with(context!!)
                        .load(tempLink)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                deleteItem(position)
                                return true
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
//                        Log.i("_loadImg", "recyclerAdapter : Load Success")
                                return false
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(it)

                }
            }

            holder.binding.itemRecyclerImageView.setOnClickListener {
                mInterface.click(position)

                /** check điều kiện reso null */
//                val options = ActivityOptionsCompat
//                    .makeSceneTransitionAnimation(
//                        context as MainActivity,
//                        holder.binding.itemHomeConstraintLayout,
//                        ViewCompat.getTransitionName(holder.binding.itemHomeConstraintLayout)!!
//                        /** check điều kiện reso null */
//                    )
            }

            holder.binding.itemRecyclerImageView.setOnLongClickListener {
                mLongClickInterface.longClick(position)
                return@setOnLongClickListener false
            }

            holder.binding.itemRecycler3dots.setOnClickListener {
                mLongClickInterface.longClick(position)
            }
        } else {
            Log.e("_info", "tempx == null , $tempx ")
        }

    }


    private fun deleteItem(position: Int) {
        mArr.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mArr.size)
    }

//    fun randomArr() {
//        mArr = Utils.randomArrStaggredFeedItems(mArr)
//        notifyDataSetChanged()
//    }

    override fun getItemCount(): Int {
        return mArr.size
    }

}