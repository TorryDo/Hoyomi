package com.torrydo.hoyomi

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.torrydo.hoyomi.databinding.FragmentBottomDialogHomeBinding
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.torrydo.hoyomi.activities.pictureActivity
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


class Fragment_bottomDialog_home : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomDialogHomeBinding
    private val mViewmodel: myViewModel by activityViewModels()

//    private val mContext = WeakReference<Context>(requireContext()).get()!!

    private val playlistArrs = ArrayList<Playlist>()
    private val staggredArrs = ArrayList<staggredFeedItems>()
    private lateinit var staggredItem: staggredFeedItems

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomDialogHomeBinding.inflate(inflater, container, false)

        refreshNativeAd()

        val staggerdBundle = arguments
        val tempStr = staggerdBundle!!.getString("mGson")
        staggredItem = Utils.getGsonInstance().fromJson(tempStr, staggredFeedItems::class.java)

        mViewmodel.allPlaylist.observe(viewLifecycleOwner, {
            if (playlistArrs.isNotEmpty()) {
                playlistArrs.clear()
            }
            it.forEach { pl ->
                playlistArrs.add(pl)
            }
        })

        binding.homeBottomSheetLine3.setOnClickListener {
            MotionToast.createColorToast(
                requireActivity(),
                "WARNING",
                getString(R.string.sorry_update_later),
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                null
            )
        }

        binding.homeBottomSheetLine1.setOnClickListener {
            var popupMenu : PopupMenu? = PopupMenu(requireContext(), binding.homeBottomSheetTxt1)
            popupMenu!!.setOnMenuItemClickListener { true }
            var menu : Menu? = popupMenu.menu
            menu!!.add(0, 0, 0, getString(R.string.menu_create_playlist))
            if (playlistArrs.isNotEmpty()) {
                for (i in 0..playlistArrs.size - 1) {
                    menu.add(1, playlistArrs[i].id, i + 1, playlistArrs[i].playlistTitle)
                }
            }

            popupMenu!!.setOnMenuItemClickListener {
                if (it.groupId == 0) {
                    if (it.itemId == 0) {

                        var alertDialog : AlertDialog? = MaterialAlertDialogBuilder(
                            requireContext(),
                            R.style.alertDialog_roundCorner
                        )
                            .setView(
                                LayoutInflater.from(requireContext())
                                    .inflate(R.layout.playlist_creator, container, false)
                            ).create()
                        alertDialog!!.show()

                        var itemCancel   : TextView? = alertDialog.findViewById<TextView>(R.id.creator_txtCancel)
                        var itemCreate   : MaterialButton? = alertDialog.findViewById<MaterialButton>(R.id.creator_buttonCreate)
                        var itemEdittext : EditText? = alertDialog.findViewById<EditText>(R.id.creator_edittext)

                        itemCancel?.setOnClickListener {
                            alertDialog?.dismiss()
                        }
                        itemCreate?.setOnClickListener {

                            val tempTitle = itemEdittext?.text.toString()

                            if (!tempTitle.isEmpty() && tempTitle.length < 25) {
                                val stagList = ArrayList<staggredFeedItems>()
                                stagList.add(staggredItem)
                                mViewmodel.addPlaylist(
                                    Playlist(
                                        0,
                                        itemEdittext?.text.toString(),
                                        Utils.getCurrentTime(),
                                        stagList,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                    )
                                )

//                                displayFinishDia(WeakReference<Context>(alertDialog.context).get()!!,"'$tempTitle' created","let's add some beautiful wallpapers","Ok")
//                                refreshNativeAd()
                                    Toast.makeText(WeakReference<Context>(alertDialog?.context).get()!!,"${itemEdittext?.text} created",Toast.LENGTH_SHORT).show()



                                if (activity != null && !requireActivity().isFinishing()) {
                                    if (nativeAd != null) {
                                        clearNativeAd()
                                    }
                                }
                                alertDialog?.dismiss()

                                alertDialog = null
                                itemCancel = null
                                itemCreate = null
                                itemEdittext = null


                            } else {

                                MotionToast.createColorToast(
                                    requireActivity(),
                                    "ERROR",
                                    getString(R.string.cant_blank_of_exceed_25chars),
                                    MotionToast.TOAST_ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    null
                                )
                            }
                        }
                        this.dismiss()
                    }


                } else if (it.groupId == 1) {

                    for (ii in 0..playlistArrs.size - 1) {
                        if (it.itemId == playlistArrs[ii].id) {
                            val tempId = playlistArrs[ii].id
                            val tempTitle = playlistArrs[ii].playlistTitle
                            val tempDate = playlistArrs[ii].date

                            if (playlistArrs[ii].list != null) {
                                playlistArrs[ii].list!!.forEach { dcm ->
                                    staggredArrs.add(dcm)
                                }
                            }
                            staggredArrs.add(staggredItem)

                            mViewmodel.updatePlaylist(
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
                            this.dismiss()

                            displayFinishDia(
                                WeakReference<Context>(requireContext()).get()!!,
                                "added to '$tempTitle'",
                                "Have fun <3",
                                "Ok"
                            )
                            refreshNativeAd()

                            break
                        }
                    }
                }

                popupMenu?.setOnDismissListener {
                    popupMenu = null
                    menu = null
                }

                popupMenu = null
                menu = null

                return@setOnMenuItemClickListener false
            }
            popupMenu?.show()
        }
        return binding.root
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

    private fun displayFinishDia(
        context: Context,
        title: String,
        content: String,
        buttonText: String
    ) {
        alertDialog =
            MaterialAlertDialogBuilder(context, R.style.alertDialog_roundCorner)
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
        adLoader = AdLoader.Builder(requireContext(), getString(R.string.nativeAds_pic))
            .forNativeAd {
                nativeAd = it

                if (nativeAd != null) {
                    Log.e("XXX", "nativeAd != null")
                    populateNativeAdView(nativeAd!!, ad)

                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    clearNativeAd()
                    adLoader = null
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

    override fun onPause() {
        super.onPause()
        if (activity != null && !requireActivity().isFinishing()) {
            if (nativeAd != null) {
                clearNativeAd()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }
}