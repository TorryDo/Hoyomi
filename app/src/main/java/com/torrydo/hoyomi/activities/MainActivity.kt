package com.torrydo.hoyomi.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.adapter.myViewPager2Adapter
import com.torrydo.hoyomi.databinding.ActivityMainBinding
import com.torrydo.hoyomi.fragment.homeFragment
import com.torrydo.hoyomi.fragment.libraryFragment
import com.torrydo.hoyomi.fragment.profileFragment
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import com.torrydo.hoyomi.viewModel.myViewmodelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.*


class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null
    var mViewModel: myViewModel? = null

    private val mContext = WeakReference<Context>(this@MainActivity).get()!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.white)

        if (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Utils.isOnline(mContext)
            } else {
                true
            }
        ) {

            var mobileDataEnabled = false // Assume disabled
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                val cmClass = Class.forName(cm.javaClass.name)
                val method: Method = cmClass.getDeclaredMethod("getMobileDataEnabled")
                method.isAccessible = true // Make the method callable
                // get the setting for "mobile data"
                mobileDataEnabled = method.invoke(cm) as Boolean
            } catch (e: Exception) {

            }

            /**  check điều kiện mạng */
            if (mobileDataEnabled) {
                Toast.makeText(mContext, getString(R.string.warning_data), Toast.LENGTH_LONG).show()
            }

            mViewModel = ViewModelProvider(
                mContext as MainActivity,
                myViewmodelFactory(application)
            ).get(myViewModel::class.java)

            val pp = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (checkCallingOrSelfPermission(pp) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(pp), 0)
                }
            }
//        checkHasSync()

            myEyes()
            defaulStuffs()
            /** ads */
            MobileAds.initialize(applicationContext) {}

            val listFrags = arrayListOf<Fragment>(
                homeFragment(),
                libraryFragment(),
                profileFragment()
            )
            binding!!.mainViewPager2.adapter =
                myViewPager2Adapter(supportFragmentManager, lifecycle, listFrags)
            binding!!.mainBottomNavView.setupWithViewPager2(binding!!.mainViewPager2)
        } else {
            MotionToast.createColorToast(
                mContext as MainActivity,
                "ERROR",
                getString(R.string.warning_required_internet),
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                null
            )
        }

    }


    private fun defaulStuffs() {

        /** load 3 loại ads */
        GlobalScope.launch(Dispatchers.Main) {
            loadInterstitial_all()
            loadInterstitial_picture()
            loadRewardAds()
        }

        refreshNativeAd()
        
    }

    val blindObserver = Observer<List<LiveWallpaper>> {}
    private fun myEyes() {

        mViewModel!!.arrayLiveWallpapers.observe(mContext as MainActivity, blindObserver)

        mViewModel!!.hasSynced.observe(mContext as MainActivity, {
            if (it) {
                checkLiveWallpaper()
            } else {
                mViewModel!!.syncFeedsFromIt()
                mViewModel!!.getAndSaveLiveWallpaper()
//                    mViewModel!!.syncingLiveWallpaperFromServer()
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isFinishing) {
            mViewModel = null
            binding?.mainViewPager2?.adapter = null
            binding?.root?.removeAllViews()
            binding = null
            finishAfterTransition()
        }
//        binding.root.removeAllViews()
//        this.finishAfterTransition()
    }

    //    fun checkHasSync(){
//        GlobalScope.launch(Dispatchers.Main) {
//            mViewModel!!.mPref.hasSync().collect {
//
//            }
//        }
//
//    }
    fun checkLiveWallpaper() {
        /** update livewallpaper mỗi 4 ngày, update ảnh sau */
        lifecycleScope.launch(Dispatchers.IO) {
            mViewModel!!.mPref.getDay().collect { lastDay ->
                withContext(Dispatchers.Main) {
                    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    Log.e("liveWallpaper", "currentDay = $currentDay & lastDay = $lastDay")
                    if (currentDay > lastDay + 4 || currentDay < lastDay) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            if (Utils.isOnline(mContext)) {

//                                mContext.cacheDir.deleteRecursively()

                                mViewModel!!.syncingLiveWallpaperFromServer()
                                mViewModel!!.setDay(currentDay)
                            }
                        }
                    }
                }
            }
        }
    }

    var rewardedAds: RewardedAd? = null
    fun clear_rewarededAds() {
        rewardedAds = null
    }

    fun loadRewardAds() {
        val adRequest = AdRequest.Builder().build()
        GlobalScope.launch(Dispatchers.Main) {
            RewardedAd.load(
                applicationContext,
                applicationContext.getString(R.string.rewardAds),
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        rewardedAds = null
                    }

                    override fun onAdLoaded(p0: RewardedAd) {
                        super.onAdLoaded(p0)
                        rewardedAds = p0
                        rewardedAds!!.setImmersiveMode(true)
                    }
                })
        }
    }

    var interstitial_all: InterstitialAd? = null
    fun clear_interstitial_all() {
        interstitial_all = null
    }

    fun loadInterstitial_all() {
        val adRequest = AdRequest.Builder().build()
        GlobalScope.launch(Dispatchers.Main) {
            InterstitialAd.load(
                applicationContext,
                applicationContext.getString(R.string.interstitial_all),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        interstitial_all = null
                    }

                    override fun onAdLoaded(p0: InterstitialAd) {
                        super.onAdLoaded(p0)
                        interstitial_all = p0
                        interstitial_all!!.setImmersiveMode(true)
                    }
                })
        }
    }

    var interstitial_picture: InterstitialAd? = null
    fun clear_interstitial_picture() {
        interstitial_picture = null
    }

    fun loadInterstitial_picture() {
        val adRequest = AdRequest.Builder().build()
        GlobalScope.launch(Dispatchers.Main) {
            InterstitialAd.load(
                applicationContext,
                applicationContext.getString(R.string.interstitial_pic),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        interstitial_picture = null
                    }

                    override fun onAdLoaded(p0: InterstitialAd) {
                        super.onAdLoaded(p0)
                        interstitial_picture = p0
                    }
                })
        }
    }

    fun displayFinishDialog(title: String, content: String, buttonText: String) {
        val alertDialog =
            MaterialAlertDialogBuilder(mContext, R.style.alertDialog_roundCorner)
                .setView(R.layout.dialog_native_ads).create()
        alertDialog.show()

        val frame = alertDialog.findViewById<FrameLayout>(R.id.mad_frame)
        val adView = layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView
        frame?.addView(adView)

        val txtTitle = alertDialog.findViewById<TextView>(R.id.mad_title)
        val txtContent = alertDialog.findViewById<TextView>(R.id.mad_content)
        val process = alertDialog.findViewById<ProgressBar>(R.id.mad_progress)
        val txtSorry = alertDialog.findViewById<TextView>(R.id.mad_txt_sorry)
        val buttonConfirm = alertDialog.findViewById<Button>(R.id.mad_button)

        txtContent?.visibility = View.VISIBLE
        process?.visibility = View.GONE
        txtSorry?.visibility = View.VISIBLE

        txtTitle?.visibility = View.VISIBLE

        txtTitle?.text = title
        buttonConfirm?.text = buttonText
        txtContent?.text = content

        val nativeAd = (mContext as MainActivity).nativeAd
        val adu = (mContext as MainActivity)
        if (nativeAd != null) {
            adu.populateNativeAdView(nativeAd, adView)
        }

        buttonConfirm?.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.setCancelable(true)

        adu.refreshNativeAd()
    }

    var nativeAd: NativeAd? = null
    var adLoader: AdLoader? = null

    fun refreshNativeAd() {
        nativeAd?.destroy()
        nativeAd = null
        loadNativeAds(layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView)
    }

    fun clearNativeAd() {
        nativeAd?.destroy()
        nativeAd = null
    }

    fun loadNativeAds(ad: NativeAdView) {
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

    fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

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


}