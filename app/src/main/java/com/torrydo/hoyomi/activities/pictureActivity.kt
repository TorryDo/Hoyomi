package com.torrydo.hoyomi.activities

import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.databinding.ActivityPictureBinding
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth
import com.torrydo.hoyomi.viewModel.myViewModel
import com.torrydo.hoyomi.viewModel.myViewmodelFactory
import kotlinx.coroutines.*
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference


class pictureActivity : AppCompatActivity() {

    private var mViewModel: myViewModel? = null
    private var binding: ActivityPictureBinding? = null

    private val mContext = WeakReference<Context>(this).get()!!
//    private val mActivity get() = WeakReference<Activity>(this).get()!!

    private lateinit var stagItem: staggredFeedItems
    private var bitmap: Bitmap? = null

//    private val imm : InputMethodManager? = null

    private var slink = CONSTANT.DEFAULT_IMG
    private val Tag = "_info"
    private var count = 0
    private var resoInt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureBinding.inflate(LayoutInflater.from(applicationContext))
        setContentView(binding?.root)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.white)
        mViewModel =
            ViewModelProvider(mContext as pictureActivity, myViewmodelFactory(application)).get(
                myViewModel::class.java
            )
        Utils.hideSystemUI(mContext as pictureActivity)

        binding!!.lottieIfinityLoad.also { lt ->
            lt.visibility = View.VISIBLE
            lt.playAnimation()
            lt.repeatCount = ValueAnimator.INFINITE
        }

        try {
            stagItem = Utils.getGsonInstance()
                .fromJson(intent.getStringExtra("wallpaper"), staggredFeedItems::class.java)
        } catch (e: Exception) {
            Log.e("_info", "pictureActivity : get data failed")
        }

        defaultStuffs()
        clickTriggers()
    }

    var b = true
    private val playlistArrs = ArrayList<Playlist>()
    private val observe_AllPlaylist = Observer<List<Playlist>> {
        if (playlistArrs.isNotEmpty()) {
            playlistArrs.clear()
        }
        it.forEach { pl ->
            playlistArrs.add(pl)
        }
    }

    private fun defaultStuffs() {

        mViewModel!!.allPlaylist.observe((mContext as pictureActivity), observe_AllPlaylist)

        binding?.pictureImageView.also { img ->
            try {
                if (stagItem.image.resolutions != null) {
                    try {
                        slink =
                            convertSmth.replaceAmp(stagItem.image.resolutions!![CONSTANT.resolution_of_StaggredItem + 2].url)

                    } catch (e: Exception) {

                        try {
                            slink =
                                convertSmth.replaceAmp(stagItem.image.resolutions!![CONSTANT.resolution_of_StaggredItem + 1].url)

                        } catch (e: Exception) {
                            try {
                                slink =
                                    convertSmth.replaceAmp(stagItem.image.resolutions!![CONSTANT.resolution_of_StaggredItem].url)
                            } catch (e: Exception) {
                                Log.e("BUGLOL", "3 times false")
                            }
                        }
                    }
                    resoInt = 0
                } else {
                    try {
                        slink = stagItem.image.source.url

                        resoInt = 1

                    } catch (e: Exception) {
                    }

                }
            } finally {
                Thread {
                    try {
                        if (resoInt == 1) {
                            try {
                                Glide.with(img!!.rootView)
                                    .asBitmap()
                                    .load(convertSmth.HightReso_to_LowReso(slink))
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .addListener(object : RequestListener<Bitmap> {
                                        override fun onResourceReady(
                                            resource: Bitmap?,
                                            model: Any?,
                                            target: Target<Bitmap>?,
                                            dataSource: DataSource?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                bitmap = resource
                                                img.setImageBitmap(bitmap)

                                                resoInt = 2
                                            }

                                            return false
                                        }

                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Bitmap>?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            return false
                                        }
                                    })
                                    .submit().get()
                            } catch (e: Exception) {
                            }
                        }
                        Glide.with(img!!.rootView)
                            .asBitmap()
                            .load(slink)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .error(
                                ContextCompat.getDrawable(
                                    mContext,
                                    R.drawable.gradient_light_pink_blue
                                )
                            )
                            .addListener(object : RequestListener<Bitmap> {
                                override fun onResourceReady(
                                    resource: Bitmap?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    bitmap = resource

                                    lifecycleScope.launch(Dispatchers.Main) {
                                        try {
//                                            Log.e("slink", "resource ready, slink = $slink")
                                            img?.setImageBitmap(
                                                resource
                                            )
                                            resoInt = 3

                                            binding!!.lottieIfinityLoad.also { lt ->
                                                lt.alpha = 0f
                                                lt.visibility = View.GONE
                                                lt.clearAnimation()
                                                lt.repeatCount = 0
                                            }
                                        } catch (e: Exception) {
                                            Log.e(
                                                "BUGLOL",
                                                "can not load image, exception = ${e.cause}"
                                            )
                                        }
                                    }
                                    return false
                                }

                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    lifecycleScope.launch(Dispatchers.Main) {

                                        binding!!.lottieIfinityLoad.also { lt ->
                                            lt.alpha = 0f
                                            lt.visibility = View.GONE
                                            lt.clearAnimation()
                                            lt.repeatCount = 0
                                        }
                                    }

                                    return false
                                }
                            })
                            .submit().get()


                    } catch (e: Exception) {

                        try {
                            Glide.with(mContext)
                                .asBitmap()
                                .load(convertSmth.HightReso_to_LowReso(slink))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .error(
                                    ContextCompat.getDrawable(
                                        mContext,
                                        R.drawable.gradient_light_pink_blue
                                    )
                                )
                                .addListener(object : RequestListener<Bitmap> {
                                    override fun onResourceReady(
                                        resource: Bitmap?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {

                                        bitmap = resource

                                        lifecycleScope.launch(Dispatchers.Main) {
                                            try {
                                                img?.setImageBitmap(
                                                    resource
                                                )
                                                Toast.makeText(
                                                    mContext,
                                                    "full resolution image may not be available now",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                binding!!.lottieIfinityLoad.also { lt ->
                                                    lt.alpha = 0f
                                                    lt.visibility = View.GONE
                                                    lt.clearAnimation()
                                                    lt.repeatCount = 0
                                                }
                                            } catch (e: Exception) {
                                                Log.e(
                                                    "BUGLOL",
                                                    "can not load image, exception = ${e.cause}"
                                                )
                                            }
                                        }
                                        return false
                                    }

                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        lifecycleScope.launch(Dispatchers.Main) {

                                            binding!!.lottieIfinityLoad.also { lt ->
                                                lt.alpha = 0f
                                                lt.visibility = View.GONE
                                                lt.clearAnimation()
                                                lt.repeatCount = 0
                                            }
                                        }
                                        return false
                                    }
                                })
                                .submit().get()

                        } catch (e: Exception) {
                            lifecycleScope.launch(Dispatchers.Main) {

                                MotionToast.createColorToast(
                                    mContext as pictureActivity,
                                    "ERROR",
                                    getString(R.string.picture_not_available),
                                    MotionToast.TOAST_ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    null
                                )
                            }
                            this@pictureActivity.finish()

                        }
                    }
                }.start()

            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                binding?.pictureMotionLayout?.transitionToEnd()
            }
        }

        refreshNativeAd()
    }


    private fun clickTriggers() {
        if (binding != null) {
            binding!!.pictureBack.setOnClickListener { onBackPressed() }

            binding!!.pictureImageView.setOnClickListener {
                if (b) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding!!.pictureMotionLayout.transitionToStart()
                        delay(200)
                        binding!!.pictureImageView.setBackgroundColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                            )
                        )
                    }
                    b = false
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding!!.pictureMotionLayout.transitionToEnd()
                        delay(200)
                        binding!!.pictureImageView.setBackgroundColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.white_gray
                            )
                        )
                    }
                    b = true
                }
            }

            // set wallpaper
            binding!!.pictureSetWallpaper.setOnClickListener {

                if (resoInt == 3) {

                    var popupMenu: PopupMenu? = PopupMenu(mContext, it)
                    var menu: Menu? = popupMenu!!.menu
                    menu?.add(0, 1, 0, CONSTANT.HOME_SCREEN)
                    menu?.add(0, 2, 0, CONSTANT.LOCK_SCREEN)
                    menu?.add(0, 3, 0, CONSTANT.BOTH_SCREEN)

                    popupMenu.setOnDismissListener {
                        popupMenu = null
                        menu?.clear()
                        menu = null
                    }

                    popupMenu?.setOnMenuItemClickListener {

                        when (it.itemId) {
                            1 -> {
                                try {
                                    if (bitmap != null) {

                                        tricker(
                                            getString(R.string.xx_setwallpaper_title),
                                            getString(R.string.xx_setwallpaper_content)
                                        )

                                        lifecycleScope.launch(Dispatchers.Main) {
                                            delay(1000L)
                                            Utils.setBitmapWallpaper(mContext, bitmap!!)
                                        }
                                    } else {

                                        MotionToast.createColorToast(
                                            mContext as pictureActivity,
                                            "ERROR",
                                            getString(R.string.picture_not_available),
                                            MotionToast.TOAST_ERROR,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            null
                                        )
                                    }
                                } catch (e: Exception) {
                                } finally {
                                    popupMenu = null
                                    menu?.clear()
                                    menu = null
                                }
                            }
                            2 -> {
                                try {
                                    if (bitmap != null) {

                                        tricker(
                                            getString(R.string.xx_setwallpaper_title),
                                            getString(R.string.xx_setwallpaper_content)
                                        )

                                        lifecycleScope.launch(Dispatchers.Main) {
                                            delay(1000L)
                                            Utils.setBitmapLockWallpaper(mContext, bitmap!!)
                                        }
                                    } else {

                                        MotionToast.createColorToast(
                                            mContext as pictureActivity,
                                            "ERROR",
                                            getString(R.string.picture_not_available),
                                            MotionToast.TOAST_ERROR,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            null
                                        )
                                    }
                                } catch (e: Exception) {
                                } finally {
                                    popupMenu = null
                                    menu?.clear()
                                    menu = null
                                }
                            }
                            3 -> {
                                try {
                                    if (bitmap != null) {

                                        tricker(
                                            getString(R.string.xx_setwallpaper_title),
                                            getString(R.string.xx_setwallpaper_content)
                                        )

                                        lifecycleScope.launch(Dispatchers.Main) {
                                            delay(1000L)
                                            Utils.setBitmapWallpaper(mContext, bitmap!!)
                                            Utils.setBitmapLockWallpaper(mContext, bitmap!!)
                                        }
                                    } else {

                                        MotionToast.createColorToast(
                                            mContext as pictureActivity,
                                            "ERROR",
                                            getString(R.string.picture_not_available),
                                            MotionToast.TOAST_ERROR,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            null
                                        )
                                    }
                                } catch (e: Exception) {
                                } finally {
                                    popupMenu = null
                                    menu?.clear()
                                    menu = null
                                }
                            }
                        }

                        return@setOnMenuItemClickListener false
                    }
                    popupMenu?.show()

                } else if (resoInt == 2) {
                    var alertDialog: AlertDialog? = MaterialAlertDialogBuilder(mContext)
                        .setTitle(getString(R.string.picture_low_resolution_title))
                        .setMessage(getString(R.string.picture_low_resolution_content))
                        .setPositiveButton(getString(R.string.picture_low_resolution_bt_positive_set)) { p0, p1 ->

                            alertDialog?.dismiss()
                            alertDialog = null

                            try {
                                if (bitmap != null) {

                                    tricker(
                                        getString(R.string.xx_setwallpaper_title),
                                        getString(R.string.xx_setwallpaper_content)
                                    )

                                    lifecycleScope.launch(Dispatchers.Main) {
                                        delay(1000L)
                                        Utils.setBitmapWallpaper(mContext, bitmap!!)
                                    }
                                } else {

                                    MotionToast.createColorToast(
                                        mContext as pictureActivity,
                                        "ERROR",
                                        getString(R.string.picture_not_available),
                                        MotionToast.TOAST_ERROR,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        null
                                    )
                                }
                            } catch (e: Exception) {
                            } finally {

                            }
                        }
                        .setNegativeButton(getString(R.string.picture_low_resolution_bt_negative)) { p0, p1 ->
                            alertDialog?.dismiss()
                            alertDialog = null
                        }
                        .create()

                    alertDialog?.setOnCancelListener {
                        alertDialog?.dismiss()
                        alertDialog = null
                    }

                    alertDialog?.show()
                } else {
                    Toast.makeText(
                        mContext,
                        getString(R.string.picture_wait_a_sec),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            // download wallpaper
            binding!!.pictureDownload.setOnClickListener {
                try {
                    if (bitmap != null) {
                        if (resoInt == 3) {

                            tricker(
                                getString(R.string.xx_download_title),
                                getString(R.string.xx_download_content)
                            )

                            lifecycleScope.launch(Dispatchers.Main) {

                                delay(1500L)

                                mViewModel?.saveToGallery(bitmap!!, CONSTANT.ALBUM_NAME)

                                delay(500L)

                                MotionToast.createColorToast(
                                    mContext as pictureActivity,
                                    "SUCCESS",
                                    getString(R.string.download_successfully),
                                    MotionToast.TOAST_SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    null
                                )

                                count++
                            }
                        } else if (resoInt == 2) {
                            var alertDialog: AlertDialog? = MaterialAlertDialogBuilder(mContext)
                                .setTitle(getString(R.string.picture_low_resolution_title))
                                .setMessage(getString(R.string.picture_low_resolution_content))
                                .setPositiveButton(getString(R.string.picture_low_resolution_bt_positive_download)) { p0, p1 ->

                                    alertDialog?.dismiss()
                                    alertDialog = null

                                    try {
                                        if (bitmap != null) {

                                            tricker(
                                                getString(R.string.xx_setwallpaper_title),
                                                getString(R.string.xx_setwallpaper_content)
                                            )

                                            lifecycleScope.launch(Dispatchers.Main) {
                                                delay(1500L)
                                                mViewModel?.saveToGallery(bitmap!!, CONSTANT.ALBUM_NAME)
                                            }
                                        } else {

                                            MotionToast.createColorToast(
                                                mContext as pictureActivity,
                                                "ERROR",
                                                getString(R.string.picture_not_available),
                                                MotionToast.TOAST_ERROR,
                                                MotionToast.GRAVITY_BOTTOM,
                                                MotionToast.LONG_DURATION,
                                                null
                                            )
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                                .setNegativeButton(getString(R.string.picture_low_resolution_bt_negative)) { p0, p1 ->
                                    alertDialog?.dismiss()
                                    alertDialog = null
                                }
                                .create()

                            alertDialog?.setOnCancelListener {
                                alertDialog?.dismiss()
                                alertDialog = null
                            }

                            alertDialog?.show()
                        }
                    } else {
                        MotionToast.createColorToast(
                            mContext as pictureActivity,
                            "ERROR",
                            getString(R.string.picture_wait_a_sec),
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            null
                        )
                    }
                } catch (e: Exception) {
                }
            }

            // save to playlist
            binding!!.pictureBookmark.setOnClickListener {

                val staggredArrs = ArrayList<staggredFeedItems>()

                val popupMenu = PopupMenu(applicationContext, it)
                popupMenu.setOnMenuItemClickListener { true }
                val menu = popupMenu.menu
                menu.add(0, 0, 0, getString(R.string.menu_create_playlist))
                if (!playlistArrs.isEmpty()) {
                    for (i in 0..playlistArrs.size - 1) {
                        menu.add(1, playlistArrs[i].id, i + 1, playlistArrs[i].playlistTitle)
                    }
                }
                popupMenu.setOnMenuItemClickListener { mn ->

                    if (mn.groupId == 0) {
                        if (mn.itemId == 0) {

                            var alertDialog2: AlertDialog? = MaterialAlertDialogBuilder(
                                mContext,
                                R.style.alertDialog_roundCorner
                            )
                                .setView(
                                    R.layout.playlist_creator
                                ).create()
                            alertDialog2!!.show()

                            var itemCancel2: TextView? =
                                alertDialog2.findViewById<TextView>(R.id.creator_txtCancel)
                            var itemCreate2: MaterialButton? =
                                alertDialog2.findViewById<MaterialButton>(R.id.creator_buttonCreate)
                            var itemEdittext2: EditText? =
                                alertDialog2.findViewById<EditText>(R.id.creator_edittext)

                            alertDialog2.setOnCancelListener {
                                alertDialog2 = null
                                itemCancel2 = null
                                itemCreate2 = null
                                itemEdittext2 = null
                            }

                            itemCancel2?.setOnClickListener {
                                alertDialog2?.dismiss()

                                alertDialog2 = null
                                itemCancel2 = null
                                itemCreate2 = null
                                itemEdittext2 = null
                            }
                            itemCreate2?.setOnClickListener {

                                val tempTitle = itemEdittext2?.text.toString()

                                if (!tempTitle.isEmpty() && tempTitle.length < 25) {
                                    val stagList = ArrayList<staggredFeedItems>()
                                    stagList.add(stagItem)
                                    mViewModel?.addPlaylist(
                                        Playlist(
                                            0,
                                            itemEdittext2?.text.toString(),
                                            Utils.getCurrentTime(),
                                            stagList,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                        )
                                    )

                                    displayFinishDialog(
                                        getString(R.string.xx_display_created_playlist_title),
                                        getString(R.string.xx_display_created_playlist_content),
                                        getString(R.string.xx_display_button)
                                    )
                                    refreshNativeAd()


                                    MotionToast.createColorToast(
                                        mContext as pictureActivity,
                                        "SUCCESS",
                                        "'$tempTitle' created",
                                        MotionToast.TOAST_SUCCESS,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        null
                                    )

                                    alertDialog2?.dismiss()

                                    alertDialog2 = null
                                    itemCancel2 = null
                                    itemCreate2 = null
                                    itemEdittext2 = null

                                } else {

                                    MotionToast.createColorToast(
                                        mContext as pictureActivity,
                                        "ERROR",
                                        getString(R.string.cant_blank_of_exceed_25chars),
                                        MotionToast.TOAST_ERROR,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        null
                                    )
                                }
                            }
                            popupMenu.dismiss()

                        }
                    } else if (mn.groupId == 1) {

                        for (ii in 0..playlistArrs.size - 1) {
                            if (mn.itemId == playlistArrs[ii].id) {
                                val tempId = playlistArrs[ii].id
                                val tempTitle = playlistArrs[ii].playlistTitle
                                val tempDate = playlistArrs[ii].date

                                if (playlistArrs[ii].list != null) {
                                    playlistArrs[ii].list!!.forEach { dcm ->
                                        staggredArrs.add(dcm)
                                    }
                                }
                                staggredArrs.add(stagItem)

                                mViewModel?.updatePlaylist(
                                    Playlist(
                                        tempId,
                                        tempTitle,
                                        tempDate,
                                        staggredArrs,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                    )
                                )
                                popupMenu.dismiss()

                                displayFinishDialog(
                                    "added to '$tempTitle'",
                                    getString(R.string.xx_addimage_to_playlist),
                                    getString(R.string.xx_addimage_button)
                                )
                                refreshNativeAd()

                                break
                            }
                        }
                    }
                    return@setOnMenuItemClickListener false
                }
                popupMenu.show()
            }

            // menu icon
            binding!!.pictureMenu.setOnClickListener {
                val popMenu = PopupMenu(mContext, it)
                val menu = popMenu.menu
                menu.add(0, 1, 1, getString(R.string.menu_information))
                menu.add(0, 2, 2, "etc")
                popMenu.setOnMenuItemClickListener {
                    if (it.itemId == 1) {

                        MotionToast.createColorToast(
                            mContext as pictureActivity,
                            "WARNING",
                            getString(R.string.sorry_update_later),
                            MotionToast.TOAST_WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            null
                        )

                    }
                    return@setOnMenuItemClickListener false
                }
                popMenu.show()
            }

        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            clearNativeAd()

            mViewModel!!.allPlaylist.removeObserver(observe_AllPlaylist)
            mViewModel = null

            Glide.get(applicationContext).clearMemory()
//            bitmap?.recycle()
            bitmap = null

            binding?.pictureImageView?.setImageBitmap(null)

            binding?.root?.removeAllViews()
            binding = null

            clearAllFindView()
        }
    }


    override fun onBackPressed() {
//        super.onBackPressed()
        finishAfterTransition()
    }

    private var nativeAd: NativeAd? = null
    private var adLoader: AdLoader? = null

    private fun refreshNativeAd() {
        nativeAd?.destroy()
        nativeAd = null
        loadNativeAds(layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView)
    }

    private fun clearNativeAd() {
        nativeAd?.destroy()
        nativeAd = null
    }

    private fun loadNativeAds(ad: NativeAdView) {
        adLoader = AdLoader.Builder(mContext, mContext.getString(R.string.nativeAds_pic))
            .forNativeAd {
                nativeAd = it

                if (nativeAd != null) {

                    Log.e("XXX", "nativeAd != null")
                    populateNativeAdView(nativeAd!!, ad)

                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("XXX", "failed to load native ads")
                }
            })
            .build()

        adLoader!!.loadAd(AdRequest.Builder().build())

    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        // Set the media view.
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
//            media.videoController.text = String.format(
//                Locale.getDefault(),
//                "Video status: Ad contains a %.2f:1 video asset.",
//                vc.aspectRatio
//            )

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
//                    refresh_button.isEnabled = true
//                    videostatus_text.text = "Video status: Video playback has ended."
//                    super.onVideoEnd()
                }
            }
        } else {
//            videostatus_text.text = "Video status: Ad does not contain a video asset."
//            refresh_button.isEnabled = true
        }
    }

    private var alertDialog: AlertDialog? = null
    private var frame: FrameLayout? = null
    private var adView: NativeAdView? = null
    private var txtTitle: TextView? = null
    private var txtContent: TextView? = null
    private var process: ProgressBar? = null
    private var txtSorry: TextView? = null
    private var buttonConfirm: Button? = null

    private fun clearAllFindView() {

        alertDialog = null
        frame = null
        adView = null
        txtTitle = null
        txtContent = null
        process = null
        txtSorry = null
        buttonConfirm = null
    }

    private fun tricker(contentText: String, confirmText: String) {
        alertDialog =
            MaterialAlertDialogBuilder(mContext, R.style.alertDialog_roundCorner)
                .setView(R.layout.dialog_native_ads).create()
        alertDialog!!.setCancelable(false)
        alertDialog!!.show()

        frame = alertDialog!!.findViewById<FrameLayout>(R.id.mad_frame)
        adView = layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView
        frame?.addView(adView)

        if (nativeAd != null) {
            populateNativeAdView(nativeAd!!, adView!!)
        }
        refreshNativeAd()

        txtTitle = alertDialog!!.findViewById<TextView>(R.id.mad_title)
        txtContent = alertDialog!!.findViewById<TextView>(R.id.mad_content)
        process = alertDialog!!.findViewById<ProgressBar>(R.id.mad_progress)
        txtSorry = alertDialog!!.findViewById<TextView>(R.id.mad_txt_sorry)
        buttonConfirm = alertDialog!!.findViewById<Button>(R.id.mad_button)

        txtContent?.visibility = View.VISIBLE
        process?.visibility = View.VISIBLE
        txtSorry?.visibility = View.INVISIBLE

        txtContent?.text = contentText
        buttonConfirm?.text = confirmText

        process?.max = 50
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 1..50) {
                delay(30L)

                withContext(Dispatchers.Main) {
                    process?.progress = i
                    if (i >= 50) {
//                        txtContent?.visibility = View.GONE
                        process?.visibility = View.GONE
                        txtSorry?.visibility = View.VISIBLE

                        txtTitle?.visibility = View.VISIBLE
                        txtTitle?.text = "Success"
                        buttonConfirm?.text = "OK"
                        txtContent?.text = getString(R.string.xx_display_created_playlist_content)

                        if (nativeAd != null) {
                            populateNativeAdView(nativeAd!!, adView!!)
                        }

                        buttonConfirm?.setOnClickListener {
                            alertDialog!!.dismiss()
                        }

                        alertDialog!!.setCancelable(true)

                    }
                }
            }
        }
    }

    private fun displayFinishDialog(title: String, content: String, buttonText: String) {
        alertDialog =
            MaterialAlertDialogBuilder(mContext, R.style.alertDialog_roundCorner)
                .setView(R.layout.dialog_native_ads).create()
        alertDialog!!.show()

        frame = alertDialog!!.findViewById<FrameLayout>(R.id.mad_frame)
        adView = layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView
        frame?.addView(adView)

        txtTitle = alertDialog!!.findViewById<TextView>(R.id.mad_title)
        txtContent = alertDialog!!.findViewById<TextView>(R.id.mad_content)
        process = alertDialog!!.findViewById<ProgressBar>(R.id.mad_progress)
        txtSorry = alertDialog!!.findViewById<TextView>(R.id.mad_txt_sorry)
        buttonConfirm = alertDialog!!.findViewById<Button>(R.id.mad_button)

        txtContent?.visibility = View.VISIBLE
        process?.visibility = View.GONE
        txtSorry?.visibility = View.VISIBLE

        txtTitle?.visibility = View.VISIBLE

        txtTitle?.text = title
        buttonConfirm?.text = buttonText
        txtContent?.text = content

        alertDialog!!.setCancelable(true)

        if (nativeAd != null) {
            populateNativeAdView(nativeAd!!, adView!!)
        }

        buttonConfirm?.setOnClickListener {
            alertDialog!!.dismiss()
            clearAllFindView()
        }

        alertDialog!!.setOnCancelListener {
            clearAllFindView()
        }

//        refreshNativeAd()
    }

    /** cancel keyboard if active */
//    private fun cancelKeyboardIfActive() {
//        if (imm.isActive) {
//            imm.hideSoftInputFromWindow(binding!!.root.windowToken, 0)
//            binding!!.searchSearchview.clearFocus()
//        }
//    }

}
