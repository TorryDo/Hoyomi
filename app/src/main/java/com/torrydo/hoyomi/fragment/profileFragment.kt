package com.torrydo.hoyomi.fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.AboutActivity
import com.torrydo.hoyomi.adapter.adapter_profile
import com.torrydo.hoyomi.databinding.FragmentProfileBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.Profile
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import kotlinx.coroutines.*
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference
import kotlin.random.Random

class profileFragment : Fragment() {

    private val mViewModel: myViewModel by activityViewModels()
    private var binding: FragmentProfileBinding? = null

    private val mContext get() = WeakReference<Context>(requireContext()).get()!!
    private val mActivity get() = WeakReference<Activity>(requireActivity()).get()!!

    private val mAdapter: adapter_profile by lazy {

        val st1 = Profile(
            getString(R.string.v1_support_dev_title),
            getString(R.string.v1_support_dev_content)
        )
        val st2 = Profile(
            getString(R.string.v2_suggestions_contact_title),
            getString(R.string.v2_suggestions_contact_content)
        )
        val st3 = Profile(
            getString(R.string.v3_reviews_title),
            getString(R.string.v3_reviews_content)
        )
        val st4 = Profile(
            getString(R.string.v4_clear_title),
            getString(R.string.v4_clear_content)
        )
        val st5 = Profile(
            getString(R.string.v5_finally_title),
            getString(R.string.v5_finally_content)
        )
        val st6 = Profile(
            getString(R.string.v6_about_title),
            getString(R.string.v6_about_content)
        )

        adapter_profile(
            mContext,
            arrayListOf(st1, st2, st3, st4, st5, st6),
            Utils.getDeviceWidth_and_Height(requireActivity(), 0),
            object : clickerInterface {
                override fun click(position: Int) {
                    when (position) {
                        0 -> {
//                            val rewardedAds = (mContext as MainActivity).rewardedAds
//                            val ma = (mContext as MainActivity)
//                            if (rewardedAds != null) {
//                                rewardedAds.fullScreenContentCallback =
//                                    object : FullScreenContentCallback() {
//                                        override fun onAdShowedFullScreenContent() {
//                                            super.onAdShowedFullScreenContent()
//                                            ma.clear_rewarededAds()
//                                            ma.loadRewardAds()
//                                        }
//                                    }
//                                rewardedAds.show(mActivity) {
//                                    Toast.makeText(
//                                        mContext,
//                                        getString(R.string.thanks_for_support),
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//
//                            }

                            MotionToast.createColorToast(mActivity,
                            "INFO",
                            getString(R.string.sorry_update_later),
                            MotionToast.TOAST_INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            null)
                        }
                        1 -> {
                            val addresses = arrayOf(getString(R.string.dev_email))
                            val subject = getString(R.string.email_title_contact)
                            val attachment: Uri? = null

                            val intent = Intent(Intent.ACTION_SEND);

                            intent.data = Uri.parse("mailto:");
                            intent.type = "message/rfc822";

//                            intent.type = "*/*";
                            intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(Intent.EXTRA_STREAM, attachment);
                            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                                startActivity(Intent.createChooser(intent, "send Email"));
                            }
                        }
                        2 -> {
                            val packageName = getString(R.string.package_name)
                            val uri = Uri.parse("market://details?id=$packageName")
                            val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                startActivity(myAppLinkToMarket)
                            } catch (e: ActivityNotFoundException) {
                                MotionToast.createColorToast(
                                    mActivity,
                                    "ERROR",
                                    "Not Available",
                                    MotionToast.TOAST_ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    null
                                )
                            }
                        }
                        3 -> {
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
//                                    val bx = PendingIntent.getBroadcast(
//                                        mContext,
//                                        100,
//                                        Intent(mContext, WallpaperReceiver::class.java),
//                                        PendingIntent.FLAG_NO_CREATE
//                                    ) == null
//                                    if (bx) {
//                                        mViewModel.clearImageFolder(CONSTANT.FILE_NAME)
//                                    }
                                    val b = mContext.cacheDir.deleteRecursively()
                                    withContext(Dispatchers.Main) {
                                        if (b) {
                                            delay(1000L)
                                            MotionToast.createColorToast(
                                                mActivity,
                                                "SUCCESS",
                                                "storage has been optimized",
                                                MotionToast.TOAST_SUCCESS,
                                                MotionToast.GRAVITY_BOTTOM,
                                                MotionToast.LONG_DURATION,
                                                null
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    MotionToast.createColorToast(
                                        mActivity,
                                        "ERROR",
                                        "error appear",
                                        MotionToast.TOAST_ERROR,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        null
                                    )
                                }
                            }
                        }
                        4 -> {
                            val rand = Random.nextInt(1, 5)
                            when (rand) {
                                1 -> Toast.makeText(
                                    mContext,
                                    "Have a Niceeee Day :3",
                                    Toast.LENGTH_SHORT
                                ).show()
                                2 -> Toast.makeText(
                                    mContext,
                                    "Wishing you all the best <3",
                                    Toast.LENGTH_SHORT
                                ).show()
                                3 -> Toast.makeText(mContext, "Never give up !", Toast.LENGTH_SHORT)
                                    .show()
                                4 -> Toast.makeText(
                                    mContext,
                                    "Always be Happy <3",
                                    Toast.LENGTH_SHORT
                                ).show()
                                else -> Toast.makeText(
                                    mContext,
                                    "Always be Happy !",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        5 -> {
                            startActivity(Intent(mContext,AboutActivity::class.java))
                        }

                    }
                }
            }
        )
    }
    private val mLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding!!.recyclerView.also {
            it.setHasFixedSize(true)
            it.adapter = mAdapter
            it.layoutManager = mLayoutManager
        }

        return binding!!.root
    }

    override fun onDestroyView() {
        if (mActivity.isFinishing) {
            binding!!.recyclerView.adapter = null
            binding!!.root.removeAllViews()
            binding = null
        }
        super.onDestroyView()
    }

}