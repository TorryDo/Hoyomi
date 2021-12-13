package com.torrydo.hoyomi.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.MainActivity
import com.torrydo.hoyomi.databinding.ItemLivewallpapersBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class liveWallpaperAdapter(
    val context: Context,
    val mArrs: ArrayList<LiveWallpaper>,
    val deviceWidth: Int,
    val mInterface: clickerInterface
) : RecyclerView.Adapter<liveWallpaperAdapter.paperHolder>() {


    class paperHolder(val binding: ItemLivewallpapersBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): paperHolder {
        val bind = ItemLivewallpapersBinding.inflate(LayoutInflater.from(context), parent, false)
        return paperHolder(bind)
    }


    override fun onBindViewHolder(holder: paperHolder, @SuppressLint("RecyclerView") position: Int) {
        try {
            val file = File(
                context.getExternalFilesDir(CONSTANT.THUMBNAILS_LIVEWALLPAPER),
                "${mArrs[position].name}.jpg"
            )
            if(file.exists()) {

                holder.binding.textView.visibility = View.GONE

                Glide.with(context).load(
                    file
                )
                    .error(ContextCompat.getDrawable(context, R.drawable.gradient_pink_blue))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Thread {
                                val b: ByteArray? =
                                    Utils.retrieveVideoFrameFromVideo(mArrs[position].link)?.let {
                                        convertSmth.bitmapToLowByteArray(
                                            it
                                        )
                                    }
                                if (b != null) {
                                    Utils.saveImgToStorage(
                                        File(
                                            context.getExternalFilesDir(CONSTANT.THUMBNAILS_LIVEWALLPAPER),
                                            mArrs[position].name + ".jpg"
                                        ), b
                                    )

                                    try {
                                        (context as MainActivity).lifecycleScope.launch(Dispatchers.Main) {
                                            Glide.with(context)
                                                .load(BitmapFactory.decodeByteArray(b, 0, b.size))
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .into(holder.binding.itemImageView)
                                        }
                                    } catch (e: Exception) {

//                                    GlobalScope.launch(Dispatchers.Main) {
//                                        holder.binding.itemImageView.setImageBitmap(
//                                            BitmapFactory.decodeByteArray(
//                                                b,
//                                                0,
//                                                b.size
//                                            )
//                                        )
//                                    }
                                        Log.e(
                                            "BUGLOL",
                                            "liveWallpaperAdapter : failed to load image from bytearray after download video frame"
                                        )


                                    }
                                }
                            }.start()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.binding.itemImageView)

            }else{
                holder.binding.textView.visibility = View.VISIBLE
                holder.binding.itemImageView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.gradient_pink_blue))

                Thread {
                    val b: ByteArray? =
                        Utils.retrieveVideoFrameFromVideo(mArrs[position].link)?.let {
                            convertSmth.bitmapToLowByteArray(
                                it
                            )
                        }
                    if (b != null) {
                        Utils.saveImgToStorage(
                            File(
                                context.getExternalFilesDir(CONSTANT.THUMBNAILS_LIVEWALLPAPER),
                                mArrs[position].name + ".jpg"
                            ), b
                        )

                        try {
                            (context as MainActivity).lifecycleScope.launch(Dispatchers.Main) {
                                Glide.with(context)
                                    .load(BitmapFactory.decodeByteArray(b, 0, b.size))
                                    .addListener(object : RequestListener<Drawable>{
                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Drawable>?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            return false
                                        }

                                        override fun onResourceReady(
                                            resource: Drawable?,
                                            model: Any?,
                                            target: Target<Drawable>?,
                                            dataSource: DataSource?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            holder.binding.textView.visibility = View.GONE
                                            return false
                                        }
                                    })
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .into(holder.binding.itemImageView)
                            }
                        } catch (e: Exception) {

                            Log.e(
                                "BUGLOL",
                                "liveWallpaperAdapter : failed to load image from bytearray after download video frame"
                            )
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            Log.e("_loadImg", "fail to load img from folder")
        }


        holder.binding.card.also { img ->

            img.requestLayout()
            img.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            img.layoutParams.height = (deviceWidth.toLong() / 2 * 1.7).toInt()

            img.setOnClickListener {
                mInterface.click(position)
            }
        }

        defaultStuffs(holder, position)
    }

    private fun defaultStuffs(holder: paperHolder, position: Int) {
        if (mArrs[position].vip) {
            holder.binding.ribbon.visibility = View.VISIBLE
            holder.binding.coin.visibility = View.VISIBLE
        } else if (!mArrs[position].vip) {
            holder.binding.ribbon.visibility = View.GONE
            holder.binding.coin.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mArrs.size
    }
}