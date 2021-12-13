package com.torrydo.hoyomi.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.torrydo.hoyomi.Fragment_bottomDialog_home
import com.torrydo.hoyomi.activities.SearchActivity
import com.torrydo.hoyomi.databinding.ItemViewpager2InLibraryBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.feedPackage.Image
import com.torrydo.hoyomi.model.feedPackage.Source
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth
import com.google.android.gms.ads.*

class search_recyclerAdapter(
    val context: Context,
    _mArr: ArrayList<staggredFeedItems>,
    val clickInterface: clickerInterface
) :
    RecyclerView.Adapter<search_recyclerAdapter.ItemHolder>() {

    val mArrs = _mArr

    class ItemHolder(val binding: ItemViewpager2InLibraryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {

        val bind =
            ItemViewpager2InLibraryBinding.inflate(LayoutInflater.from(context), parent, false)
        return ItemHolder(bind)

    }

//    override fun onViewAttachedToWindow(holder: ItemHolder) {
//        super.onViewAttachedToWindow(holder)
////        loadNativeAds(holder)
//    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        val slink = convertSmth.HightReso_to_LowReso(mArrs[position].image.source.url)
//        Log.e("_info", "search slink = $slink")
        try {

            Glide.with(context).load(slink)
//                .listener(object : RequestListener<Drawable> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: Target<Drawable>?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        Glide.with(context).load(slink)
//                            .diskCacheStrategy(DiskCacheStrategy.NONE)
//                            .into(holder.binding.image)
//                        return false
//                    }
//
//                    override fun onResourceReady(
//                        resource: Drawable?,
//                        model: Any?,
//                        target: Target<Drawable>?,
//                        dataSource: DataSource?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//
//                        return false
//                    }
//                })
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.binding.image)
        }catch (e:Exception){
            Log.e("BUGLOL","search recyclerview failed to load image at position = $position")
        }

        holder.binding.image.setOnClickListener {
            clickInterface.click(position)
        }
        holder.binding.image.setOnLongClickListener {
            val _tempStr = mArrs[position].image.source.url

            val bottomFrag = Fragment_bottomDialog_home()

            val bundle = Bundle()
            val tempStr = Utils.getGsonInstance().toJson(
                staggredFeedItems(
                    0,
                    "search",
                    "null",
                    Image(
                        null,
                        Source(
                            1920,
                            _tempStr,
                            1080
                        )
                    ),
                    "null",
                    false
                )
            )
            bundle.putString("mGson", tempStr)
            bottomFrag.arguments = bundle

            val fm = (context as SearchActivity).supportFragmentManager
            bottomFrag.show(fm, "bottom")

            return@setOnLongClickListener false
        }


    }

    override fun getItemCount(): Int {
        return mArrs.size
    }

}