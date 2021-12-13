package com.torrydo.hoyomi.fragment.insideHome

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.MainActivity
import com.torrydo.hoyomi.adapter.liveWallpaperAdapter
import com.torrydo.hoyomi.databinding.FragmentHomeLivewallpaperBinding
import com.torrydo.hoyomi.fragment.insideLiveWallpaper.VideoFragment
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.joery.animatedbottombar.AnimatedBottomBar
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference

class home_liveWallpaperFragment : Fragment() {

    private val mytag = "fromLivewallpaper"
    private var myPosition = 0
    private var count = 0

    private var binding: FragmentHomeLivewallpaperBinding? = null
    private val mViewModel: myViewModel by activityViewModels()

    private val mContext get() = WeakReference<Context>(requireContext()).get()!!
    private val mActivity get() = WeakReference<Activity>(requireActivity()).get()!!

    private val gridLayoutManager: GridLayoutManager by lazy {
        GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false)
    }
    private var mAdapter : liveWallpaperAdapter? = null

    private val mObserver_refreshPage = Observer<Boolean> {
        if (it == true) {

            MotionToast.createColorToast(mActivity,
                "WARNING",
                "Not supported on this page",
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                null
            )

//            Utils.randomArrLiveWallpaper(arrayLiveWallpapers).forEach {
//                arrayLiveWallpapers.add(it)
//            }
//
//            mAdapter?.notifyDataSetChanged()
            mViewModel.refreshPage.value = false

        }
    }
    private val mObserver_scrolltoTop = Observer<Boolean> {
        if (it == true) {
            binding?.homeLiveRecyclerView?.smoothScrollToPosition(0)
            mViewModel.scrolltoTop.value = false
        }
    }

//    init {
//        gridLayoutManager =
//    }

    var arrayLiveWallpapers = ArrayList<LiveWallpaper>()
    private val Observer_LiveWallpapers = Observer<List<LiveWallpaper>> { list ->
//        val newArr = ArrayList<LiveWallpaper>()
//        newArr.addAll(list)
        if(arrayLiveWallpapers.isNotEmpty()){
            arrayLiveWallpapers.clear()
        }
//        Utils.randomArrLiveWallpaper(newArr).forEach {
//            arrayLiveWallpapers.add(it)
//        }
        arrayLiveWallpapers.addAll(list)

        mAdapter?.notifyDataSetChanged()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeLivewallpaperBinding.inflate(inflater)

//        loadInterstitialAds()
        defaultStuffs()
        myEyes()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.clearRefresh_and_scroll()
        mViewModel.refreshPage.observe(viewLifecycleOwner, mObserver_refreshPage)
        mViewModel.scrolltoTop.observe(viewLifecycleOwner, mObserver_scrolltoTop)

//        Log.e("YYY", "mArrs size = ${mArrs.size} ")
    }

    override fun onPause() {

        mViewModel.scrolltoTop.removeObserver(mObserver_scrolltoTop)
        mViewModel.refreshPage.removeObserver(mObserver_refreshPage)

        super.onPause()
    }

    private fun myEyes() {
        mViewModel.arrayLiveWallpapers.observe(viewLifecycleOwner, Observer_LiveWallpapers)
    }

    private fun defaultStuffs() {
        mAdapter = liveWallpaperAdapter(
            mContext,
            arrayLiveWallpapers,
            Utils.getDeviceWidth_and_Height(mActivity, 0),
            object : clickerInterface {
                override fun click(position: Int) {

                    if(count > 3){
                        count = 0
                    }
                    myPosition = position

                    if (count == 0) {
                        val picAds = (mContext as MainActivity).interstitial_picture

                        if (picAds != null) {
                            picAds.fullScreenContentCallback = object :
                                FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    moveToVideoFragment(position)
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    moveToVideoFragment(position)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    (mContext as MainActivity).clear_interstitial_picture()
                                    (mContext as MainActivity).loadInterstitial_picture()
                                }
                            }

                            picAds.show(mActivity)
                        } else {
                            moveToVideoFragment(myPosition)
                        }
                    } else {
                        moveToVideoFragment(myPosition)
                    }

                    count++

                }
            }
        )

        binding?.homeLiveRecyclerView.also { recycler ->
            recycler?.setHasFixedSize(true)
            recycler?.adapter = mAdapter
            recycler?.layoutManager = gridLayoutManager
        }
    }

    override fun onDestroyView() {
        if(mActivity.isFinishing){
            mAdapter = null
            binding!!.homeLiveRecyclerView.adapter = null
            binding!!.root.removeAllViews()
            binding = null
        }
        super.onDestroyView()
    }


    private fun moveToVideoFragment(position: Int) {
        requireActivity().findViewById<FragmentContainerView>(R.id.myFrame).visibility = View.VISIBLE

        mActivity.findViewById<AnimatedBottomBar>(R.id.main_bottomNavView).visibility = View.GONE

        mViewModel.liveWallpaperPosition.value = position

        (mActivity as MainActivity).supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<VideoFragment>(R.id.myFrame)
            addToBackStack("main")
        }
//        findNavController().navigate(R.id.action_home_liveWallpaperFragment_to_videoFragment)
    }
}