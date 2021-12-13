package com.torrydo.hoyomi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.utils.LottieValueAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.pictureActivity
import com.torrydo.hoyomi.databinding.ItemVideoBinding
import com.torrydo.hoyomi.interfaces.retrieveVideoDownloadRequest
import com.torrydo.hoyomi.model.LiveWallpaper
import www.sanju.motiontoast.MotionToast
import java.io.File

class adapter_video(
    val context: Context,
    val mArrs: ArrayList<LiveWallpaper>,
    val mInterface: retrieveVideoDownloadRequest
) :
    RecyclerView.Adapter<adapter_video.videoHolder>() {

    class videoHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): videoHolder {
        val bind = ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false)
        return videoHolder(bind)
    }


    override fun onViewDetachedFromWindow(holder: videoHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.binding.imageView.visibility = View.VISIBLE
        holder.binding.dashboard.visibility = View.VISIBLE
        holder.binding.videoView.stopPlayback()

    }

    override fun onBindViewHolder(holder: videoHolder, position: Int) {

        val vid = holder.binding.videoView

        Glide.with(context)
            .load(
                File(
                    context.getExternalFilesDir(CONSTANT.THUMBNAILS_LIVEWALLPAPER),
                    "${mArrs[position].name}.jpg"
                )
            )
            .error(ContextCompat.getDrawable(context, R.drawable.gradient_light_pink_blue))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.binding.imageView)


        vid.setVideoPath(mArrs[position].link)
        vid.setOnPreparedListener { mediaPlayer ->

            val duration = vid.duration
            if (duration > 0) {

                holder.binding.itemvidLottieIfinityLoad.also {
                    it.visibility = View.GONE
                    it.clearAnimation()
                    it.repeatCount = 0
                }

                mediaPlayer?.isLooping = true

                val animate = AnimationUtils.loadAnimation(context, R.anim.exit_fade)

                animate.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {
                        vid.start()
                        vid.alpha = 1f
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        holder.binding.imageView.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(p0: Animation?) {}
                })
                holder.binding.imageView.startAnimation(animate)

            }
        }
        clicker(holder, position)
        defaultStuffs(holder, position)
    }

    private fun defaultStuffs(holder: videoHolder, position: Int) {

        holder.binding.itemvidLottieIfinityLoad.also {
            it.visibility = View.VISIBLE
            it.playAnimation()
            it.repeatCount = LottieValueAnimator.INFINITE
        }

        if (mArrs[position].unlocked) {
            holder.binding.download.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_download
                )
            )
        } else {
            holder.binding.download.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_crown_coin
                )
            )
        }

    }

    private fun clicker(holder: videoHolder, position: Int) {
        val dashboard = holder.binding.dashboard
        val info = holder.binding.info
        val download = holder.binding.download
        val minimize = holder.binding.minimize
        val bottomTri = holder.binding.bottomTriangle

        minimize.setOnClickListener {
            Toast.makeText(context, "update later, sorry for invconvenient :<", Toast.LENGTH_SHORT)
                .show()
        }

        holder.binding.root.setOnClickListener {
            holder.binding.dashboard.visibility = View.VISIBLE
        }

        info.setOnClickListener {
            Toast.makeText(context, "update later, sorry for invconvenient :<", Toast.LENGTH_SHORT)
                .show()
        }

        download.setOnClickListener {
            mInterface.clicked(position, holder)

        }

        minimize.setOnClickListener {

        }

        bottomTri.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(context, R.anim.center_down)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    dashboard.visibility = View.GONE
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
            dashboard.startAnimation(animation)
        }

    }

    override fun getItemCount(): Int {
        return mArrs.size
    }

}