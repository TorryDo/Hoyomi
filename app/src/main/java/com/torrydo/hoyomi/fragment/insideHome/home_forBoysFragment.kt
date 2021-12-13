package com.torrydo.hoyomi.fragment.insideHome

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.Fragment_bottomDialog_home
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.MainActivity
import com.torrydo.hoyomi.activities.pictureActivity
import com.torrydo.hoyomi.adapter.recyclerAdapter
import com.torrydo.hoyomi.databinding.FragmentHomeForboysBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.interfaces.longClickInterface
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference


class home_forBoysFragment : Fragment() {


    private var binding: FragmentHomeForboysBinding? = null
    private val mViewModel: myViewModel by activityViewModels()

    private val mContext get() = WeakReference<Context>(requireContext()).get()!!
    private val mActivity get() = WeakReference<Activity>(requireActivity()).get()!!

    private var mAdapter: recyclerAdapter? = null
    private val mStaggeredLayoutManager: StaggeredGridLayoutManager by lazy {
        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }
    private var mArrs = ArrayList<staggredFeedItems>()

    private val mytag = "fromForboys"
    private var myPosition = 0

    private var mcount = 0

    fun moveToPictureActivity(position: Int) {
        Intent(mContext, pictureActivity::class.java).also {
            it.putExtra(
                "wallpaper",
                Utils.getGsonInstance().toJson(mArrs[position])
            )
            it.putExtra(CONSTANT.FROM_FORBOYS, CONSTANT.FROM_FORBOYS)
            mContext.startActivity(it)
        }
    }



    private val arrayForBoys = Observer<List<staggredFeedItems>> { stagList ->
        if (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Utils.isOnline(mContext)
            } else {
                true
            }
        ) {
            var tempArrs = ArrayList<staggredFeedItems>()
            if (tempArrs.isNotEmpty()) {
                tempArrs.clear()
            }
            tempArrs.addAll(stagList)

            if (!mArrs.isNullOrEmpty()) {
                mArrs.clear()
            }

            Utils.randomArrStaggredFeedItems(tempArrs).forEach {
                mArrs.add(it)
            }

            mAdapter?.notifyDataSetChanged()
        } /*else {
            MotionToast.createColorToast(
                mActivity,
                "ERROR",
                getString(R.string.warning_turnon_internet),
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                null
            )
        }*/


    }
    private val mObserver_refreshPage = Observer<Boolean> {
        if (it == true) {
            /** LỖI Ở ĐÂY NÀI .. why , ko delete mArrs nhưng khi refresh mArr.size = 944. lẽ ra nó phải nhiều hơn chứ clgt ??
             *   khi nào xong app xem lại cho tau
             * */

            Utils.randomArrStaggredFeedItems(mArrs).forEach {
                mArrs.add(it)
            }

            mAdapter?.notifyDataSetChanged()
            mViewModel.refreshPage.value = false

        }
    }
    private val mObserver_scrolltoTop = Observer<Boolean> {
        if (it == true) {
            binding?.homeForBoysRecyclerView?.smoothScrollToPosition(0)
            mViewModel.scrolltoTop.value = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeForboysBinding.inflate(inflater, container, false)
        defaultStuffs()
        myEyes()

        return binding?.root
    }

    private fun defaultStuffs() {
        /** create mAdapter */
        mAdapter = recyclerAdapter(
            mContext,
            mArrs,
            Utils.getDeviceWidth_and_Height(requireActivity(), 0),
            Utils.getDeviceWidth_and_Height(requireActivity(), 1),
            object : clickerInterface {
                override fun click(position: Int) {
                    if (mcount > 3) {
                        mcount = 0
                    }

                    myPosition = position

                    if (mcount == 0) {

                        val picAds = (mContext as MainActivity).interstitial_picture
                        if (picAds != null) {

                            picAds.fullScreenContentCallback = object :
                                FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    moveToPictureActivity(position)
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    moveToPictureActivity(position)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    (mContext as MainActivity).clear_interstitial_picture()
                                    (mContext as MainActivity).loadInterstitial_picture()
                                }
                            }

                            picAds.show(mActivity)

                            (mContext as MainActivity).clear_interstitial_picture()
                            (mContext as MainActivity).loadInterstitial_picture()
//                                loadInterstitialAds()

                        } else {
                            moveToPictureActivity(position)
                        }
                    } else {
                        moveToPictureActivity(position)
                    }

                    mcount++

                }
            },
            object : longClickInterface {
                override fun longClick(position: Int) {
                    showOptionDialog(position)
                }

            }
        )
//        mStaggered =
//        mStaggered.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE

        binding?.homeForBoysRecyclerView.also {

//            it?.setItemViewCacheSize(20)
//            it?.isDrawingCacheEnabled = true
//            it?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

            it?.setHasFixedSize(true)
            it?.layoutManager = mStaggeredLayoutManager
            it?.adapter = mAdapter
        }
//        Log.e("YYY", "đã set adapter")
    }

    private fun myEyes() {
        /**  */
        mViewModel.hasSynced.observe(viewLifecycleOwner, {
            if (it) {
                mViewModel.arrayForBoys.observe(viewLifecycleOwner, arrayForBoys)
            }
        })

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

    override fun onDestroyView() {
        super.onDestroyView()
        if (mActivity.isFinishing) {
            mAdapter = null
            binding!!.homeForBoysRecyclerView.adapter = null
            binding!!.root.removeAllViews()
            binding = null
        }
    }

    fun showOptionDialog(position: Int) {
        val bottomFrag = Fragment_bottomDialog_home()

        val bundle = Bundle()
        val tempStr = Utils.getGsonInstance().toJson(mArrs[position])
        bundle.putString("mGson", tempStr)
        bottomFrag.arguments = bundle

        val fm = (context as MainActivity).supportFragmentManager
        bottomFrag.show(fm, "bottom")
    }


}