package com.torrydo.hoyomi.activities

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.adapter.search_recyclerAdapter
import com.torrydo.hoyomi.databinding.ActivitySearchBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.feedPackage.Image
import com.torrydo.hoyomi.model.feedPackage.Source
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.viewModel.myViewModel
import com.torrydo.hoyomi.viewModel.myViewmodelFactory
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.torrydo.hoyomi.utility.Utils
import java.lang.ref.WeakReference
import kotlin.random.Random

class SearchActivity : AppCompatActivity() {

    private var mViewmodel: myViewModel? = null
    private var binding: ActivitySearchBinding? = null

    private val mContext get() = WeakReference<Context>(this).get()!!
//    private val mActivity : WeakReference<SearchActivity> = WeakReference<SearchActivity>(this)

    private var mArrs = ArrayList<staggredFeedItems>()

    //    var count = 1
    var searchKey = "hoyomi"
//    var count = 0

    private var searchAdapter: search_recyclerAdapter? = null
    private val layoutManager: GridLayoutManager by lazy {
        GridLayoutManager(mContext, 2)
    }

    private val imm: InputMethodManager by lazy {
        mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private var alertDialog : AlertDialog? = null
    private var frame : FrameLayout? = null
    private var adView : NativeAdView? = null

    private fun clearAllFindView0() {
        alertDialog = null
        frame = null
        adView = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(LayoutInflater.from(mContext))
        setContentView(binding!!.root)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.white)
        mViewmodel =
            ViewModelProvider(mContext as SearchActivity, myViewmodelFactory(application)).get(
                myViewModel::class.java
            )

        searchAdapter =
            search_recyclerAdapter(mContext as SearchActivity, mArrs, object : clickerInterface {
                override fun click(position: Int) {

                    alertDialog =
                        MaterialAlertDialogBuilder(mContext, R.style.alertDialog_roundCorner)
                            .setView(R.layout.dialog_native_ads).create()
                    alertDialog!!.show()

                    frame = alertDialog!!.findViewById<FrameLayout>(R.id.mad_frame)
                    adView = layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView
                    frame?.addView(adView)
                    if (nativeAd != null) {
                        populateNativeAdView(nativeAd!!, adView!!)
                    }

                    val skipButton = alertDialog!!.findViewById<Button>(R.id.mad_button)
                    skipButton?.setOnClickListener {
                        alertDialog!!.dismiss()


                        Intent(mContext, pictureActivity::class.java).also { mIntent ->
                            val tempStr = mArrs[position]
                            clearAllFindView0()

                            mIntent.putExtra(
                                "wallpaper",
                                Utils.getGsonInstance()
                                    .toJson(tempStr)
                            )
                            startActivity(mIntent)
                        }
                    }
                    alertDialog!!.setOnCancelListener {
                        clearAllFindView0()
                    }
                    /** refresh nativeAds */
                    refreshNativeAds()


//                    } else {
//                        val tempStr = mArrs[position]
//                        Intent(mContext, pictureActivity::class.java).also {
//
//                            it.putExtra(
//                                "wallpaper",
//                                com.torrydo.hoyomi.utility.Utils.getGsonInstance().toJson(tempStr)
//                            )
//                            mContext.startActivity(it)
//                        }
//                    }
//                    count++
                }
            })

        mViewmodel!!.arraySearchLink.observe(mContext as SearchActivity, { arr ->

//            if (mArrs.isNotEmpty()) {
//                mArrs.clear()
//            }

            binding?.txtMaybe?.visibility = View.GONE

            arr.forEach { str ->
                mArrs.add(
                    staggredFeedItems(
                        0,
                        getString(R.string.default_stag_search_name),
                        getString(R.string.default_stag_search_title),
                        Image(null, Source(1920, str, 1080)),
                        getString(R.string.default_stag_search_feedsource),
                        false
                    )
                )
            }

            /** bug vai ln that */
//            mArrs = com.torrydo.hoyomi.utility.Utils.randomArrStaggredFeedItems(mArrs)


            /* hide title & invisible process,ads */
            nativeAd?.destroy()
            binding!!.frameAds.visibility = View.GONE
            loadNativeAds(layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView)

            binding!!.searchIncludeToolbar.customToolbarTxtTitle.also { txt ->
                txt.visibility = View.VISIBLE
            }
            binding!!.lottieIfinityLoad.also { line ->
                line.visibility = View.GONE
                line.clearAnimation()
                line.repeatCount = 0
            }

            searchAdapter?.notifyDataSetChanged()


        })

        //set recyclerview
        binding!!.searchRecyclerView.also {
            it.adapter = searchAdapter
            it.layoutManager = layoutManager
        }

        binding!!.searchSearchview.also {
            /** show keyboard */
            it.requestFocusFromTouch()
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        /** load more search item if current item >= lastItem -5 */
        binding!!.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastItemVisible = layoutManager.findLastVisibleItemPosition()
                if (lastItemVisible >= mArrs.size - 5) {
                    mViewmodel!!.appendSearchingTree()
                }
            }
        })
//        MobileAds.initialize(mContext)

        val frame = binding!!.frameAds
        val adView = layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView
        frame.addView(adView)

        loadNativeAds(adView)

        clickTriggers()
        defaultStuff()
    }

    fun refreshNativeAds() {
        if (nativeAd != null) {
            nativeAd!!.destroy()
            nativeAd = null
            loadNativeAds(layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView)
        }
    }

    override fun onPause() {
        super.onPause()
        cancelKeyboardIfActive()
        if (isFinishing) {
            mViewmodel = null
            searchAdapter = null

            binding!!.searchRecyclerView.adapter = null
            binding!!.root.removeAllViews()
            binding = null

            clearAllFindView0()
            clearNativeAd()
        }
    }

    private var nativeAd: NativeAd? = null
    private var adLoader: AdLoader? = null

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

    private fun searching(search: String) {
        binding!!.searchIncludeToolbar.customToolbarTxtTitle.also { txt ->
            txt.visibility = View.INVISIBLE
            txt.text = search
        }
        binding!!.lottieIfinityLoad.also { line ->

            line.visibility = View.VISIBLE
            line.playAnimation()
            line.repeatCount = ValueAnimator.INFINITE

        }


        if (mArrs.isNotEmpty()) {
            mArrs.clear()
        }

        mViewmodel!!.searchingTheBest(search.replace(" ", "+"))
        binding!!.searchSearchview.setText("")
    }

    private fun clickTriggers() {

        /** searching */
        binding!!.searchButton.setOnClickListener {
            searchKey = binding!!.searchSearchview.text.toString()
//            if (searchKey.isNotEmpty() || searchKey.length > 40) {

//                if (count == 1) {
//                lifecycleScope.launch() {
//                    if (interstitialAds != null) {
//                        interstitialAds?.show(mContext as SearchActivity)
//                        searching(searchKey)
//                        interstitialAds = null
//
//                    } else {
//                        searching(searchKey)
//                    }
                searching(searchKey)

//                }
//                } else { searching(searchKey) }
//            } else {
//                Toast.makeText(
//                    mContext,
//                    getString(R.string.cant_blank_of_exceed_25chars),
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
            cancelKeyboardIfActive()
        }

        binding!!.searchIncludeToolbar.customToolbarIconBack.setOnClickListener {
            super.onBackPressed()
        }
    }


    override fun onBackPressed() {
        cancelKeyboardIfActive()
        super.onBackPressed()
    }

    private fun cancelKeyboardIfActive() {
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(binding!!.root.windowToken, 0)
            binding!!.searchSearchview.clearFocus()
        }
    }

    private fun defaultStuff() {
        binding!!.searchIncludeToolbar.customToolbarTxtTitle.text = getString(R.string.search_title)

        val mSearch = binding!!.searchSearchview

        when (Random.nextInt(1, 6)) {
            1 -> {
                mSearch.hint = "Miku Nakano"
            }
            2 -> {
                mSearch.hint = "Attack on Titan"
            }
            3 -> {
                mSearch.hint = "Naruto"
            }
            4 -> {
                mSearch.hint = "One Piece"
            }
            5 -> {
                mSearch.hint = "Zerotwo"
            }
        }
    }
}