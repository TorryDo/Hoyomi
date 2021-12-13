package com.torrydo.hoyomi.fragment.insideLiveWallpaper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.MainActivity
import com.torrydo.hoyomi.adapter.adapter_video
import com.torrydo.hoyomi.databinding.FragmentVideoBinding
import com.torrydo.hoyomi.interfaces.retrieveVideoDownloadRequest
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import com.google.android.gms.ads.*
import com.torrydo.hoyomi.activities.pictureActivity
import com.torrydo.hoyomi.fragment.insideHome.home_liveWallpaperFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.joery.animatedbottombar.AnimatedBottomBar
import www.sanju.motiontoast.MotionToast
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


class VideoFragment : Fragment() {

    private val mytag = "fromVideo"
    private var position = 0

    private val mViewModel: myViewModel by activityViewModels()
    private var binding: FragmentVideoBinding? = null

    private val mContext get() = WeakReference<Context>(requireContext()).get()!!
    private val mActivity get() = WeakReference<Activity>(requireActivity()).get()!!

    private var mArrs = ArrayList<LiveWallpaper>()
    private val ObserveLiveWallpaper = Observer<List<LiveWallpaper>> { a ->
        if (mArrs.isNotEmpty()) {
            mArrs.clear()
        }

        mArrs.addAll(a)
        mAdapter.notifyDataSetChanged()
    }
    private val mAdapter: adapter_video by lazy {

        adapter_video(mContext, mArrs, object : retrieveVideoDownloadRequest {
            override fun clicked(position: Int, holder: adapter_video.videoHolder) {

                holder.binding.videoView.pause()

                if (!mArrs[position].unlocked) {

                    val rewardedAds = (mContext as MainActivity).rewardedAds
                    val interstitial_all = (mContext as MainActivity).interstitial_all
                    val ma = (mContext as MainActivity)

                    /** load ads */
                    if (rewardedAds != null) {

                        rewardedAds.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()

                                    interstitial_all?.fullScreenContentCallback =
                                        object : FullScreenContentCallback() {
                                            override fun onAdShowedFullScreenContent() {
                                                super.onAdShowedFullScreenContent()

                                                ma.clear_interstitial_all()
                                                ma.loadInterstitial_all()
                                            }
                                        }
                                    interstitial_all?.show(mActivity)

                                    holder.binding.videoView.resume()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    ma.clear_rewarededAds()
                                    ma.loadRewardAds()
                                }
                            }

                        rewardedAds.show(mActivity) { p0 ->

                            /** khi user nhận reward */

                            val id = mArrs[position].id
                            val name = mArrs[position].name
                            val vip = mArrs[position].vip
                            val link = mArrs[position].link
                            val unlocked = mArrs[position].unlocked

                            if (!unlocked) {
                                val tempLive = LiveWallpaper(id, name, vip, link, true)
                                mViewModel.updateLiveWallpaper(tempLive)
                            }
                            holder.binding.download.setImageDrawable(
                                ContextCompat.getDrawable(
                                    mContext.applicationContext,
                                    R.drawable.ic_download
                                )
                            )
                        }
                    } else {
                        Toast.makeText(mContext, getString(R.string.preparing_resource), Toast.LENGTH_SHORT).show()

                    }

                } else {
                    holder.binding.download.also {
                        it.isEnabled = false
                        it.alpha = 0.5f
                    }
                    /** đã thay thread -> Dispatcher.IO */
                    lifecycleScope.launch(Dispatchers.IO) {
                        downloadFile(mArrs[position].link, CONSTANT.TEMP_LIVEWALLPAPER, holder)
                    }
                }
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        myEyes()
        defaultStuffs()

        /** config viewPager2 */
        binding?.viewPager2.also { rec ->
            rec?.adapter = mAdapter
        }

        return binding?.root
    }

    private fun myEyes() {
        mViewModel.arrayLiveWallpapers.observe(viewLifecycleOwner, ObserveLiveWallpaper)
        mAdapter?.notifyDataSetChanged()
        /** get livewallpaper position */
        mViewModel.liveWallpaperPosition.observe(viewLifecycleOwner, {
            position = it
            binding?.viewPager2?.setCurrentItem(position, false)
        })
    }

    private fun defaultStuffs() {

        Utils.setWhiteStatusTextColor(true, requireActivity())

        /** back press */
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
//                    mActivity.findViewById<ConstraintLayout>(R.id.mainConstraint).visibility =
//                        View.VISIBLE

                    mActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                    (mActivity as MainActivity).supportFragmentManager.beginTransaction()
                        .remove(WeakReference<Fragment>(this@VideoFragment).get()!!).commit()
                    Utils.setWhiteStatusTextColor(false, mActivity)

                    this@VideoFragment.onDestroyView()
                }
            })

//        lifecycleScope.launch(Dispatchers.Main) {
//            loadRewardsAds()
//        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mActivity.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        mActivity.findViewById<AnimatedBottomBar>(R.id.main_bottomNavView).visibility = View.VISIBLE
        if (isRemoving) {
            binding!!.viewPager2.adapter = null
            binding!!.root.removeAllViews()
            binding = null
        }
        super.onDestroyView()

    }

    private fun downloadFile(url: String, albumName: String, holder: adapter_video.videoHolder) {
        val filename = "${R.string.app_name}_${System.currentTimeMillis()}.mp4"
        val dirPath =
            File(
                "${mContext.getExternalFilesDir(albumName)}",
                filename
            ).absolutePath.toString()
        Log.e("XXX", "dirpath = ${dirPath}")

        PRDownloader.initialize(mContext.applicationContext)
        PRDownloader.download(url, dirPath, filename)
            .build()
            .setOnStartOrResumeListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    holder.binding.process.visibility = View.VISIBLE
                }
            }
            .setOnProgressListener {
                holder.binding.process.max = it.totalBytes.toInt()
                holder.binding.process.progress = it.currentBytes.toInt()
//                Log.e("_process", it.toString())

            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    lifecycleScope.launch(Dispatchers.Main) {

                        Utils.saveMediaFileToGallery(
                            mContext,
                            filename,
                            dirPath
                        )
                        holder.binding.process.visibility = View.GONE

                        holder.binding.download.also {
                            it.isEnabled = true
                            it.alpha = 1f
                        }

                        // im not sure about this :3
                        (mContext as MainActivity).displayFinishDialog(
                            getString(R.string.xx_display_downloaded_title),
                            getString(R.string.xx_display_downloaded_content),
                            getString(R.string.xx_display_downloaded_button)
                        )

                        MotionToast.createColorToast(mActivity,
                            "SUCCESS",
                            getString(R.string.download_successfully),
                            MotionToast.TOAST_SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            null
                        )

                        holder.binding.videoView.resume()

                    }
                }

                override fun onError(error: Error?) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MotionToast.createColorToast(mActivity,
                            "ERROR",
                            getString(R.string.error),
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            null
                        )
                    }
                    Log.e("_info", "dirPath = $dirPath \n fileName = $filename \n error = $error")
                }
            })
    }
}


