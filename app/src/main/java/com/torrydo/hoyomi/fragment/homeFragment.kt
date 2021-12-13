package com.torrydo.hoyomi.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.airbnb.lottie.utils.LottieValueAnimator
import com.torrydo.hoyomi.activities.SearchActivity
import com.torrydo.hoyomi.adapter.myViewPager2Adapter
import com.torrydo.hoyomi.databinding.FragmentHomeBinding
import com.torrydo.hoyomi.fragment.insideHome.home_forBoysFragment
import com.torrydo.hoyomi.fragment.insideHome.home_liveWallpaperFragment
import com.torrydo.hoyomi.viewModel.myViewModel
import java.lang.ref.WeakReference
import java.util.*

class homeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val mViewModel: myViewModel by activityViewModels()

//    private val mContext = WeakReference<Context>(requireContext()).get()!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.homeViewpagerbot.also {
            val tempArr = arrayListOf<Fragment>(
                home_forBoysFragment(),
                home_liveWallpaperFragment()/*,
                home_forGirlsFragment()*/

            )
            val tempAdapter = myViewPager2Adapter(childFragmentManager, lifecycle, tempArr)

            it.adapter = tempAdapter
            it.isUserInputEnabled = false

            chipClickListener()

        }

//        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//        if(currentHour < 12) {
            binding.homeChipForBoys.isChecked = true
            binding.homeViewpagerbot.currentItem = 0
//        }else{
//            binding.homeChipLiveWallpapers.isChecked = true
//            binding.homeViewpagerbot.currentItem = 1
//        }


        myEyes()
        doStuffs()
        return binding.root
    }

    private fun chipClickListener() {
        binding.homeChipForBoys.setOnClickListener {
            binding.homeViewpagerbot.currentItem = 0
        }
        binding.homeChipLiveWallpapers.setOnClickListener {
            binding.homeViewpagerbot.currentItem = 1
        }
//        binding.homeChipForGirls.setOnClickListener {
//            binding.homeViewpagerbot.currentItem = 2
//        }
    }

    private fun doStuffs() {

        binding.homeSearchview.setOnClickListener {
            val i = Intent(requireContext(), SearchActivity::class.java)
            startActivity(i)
        }
        binding.homeLottieLoadingVaporize.setOnClickListener {

            mViewModel.refreshPage.value = true
//            binding.homeLottieLoadingVaporize.playAnimation()

        }
        binding.homeLottieUpArrow.setOnClickListener {

            mViewModel.scrolltoTop.value = true
//            binding.homeLottieUpArrow.playAnimation()

        }


        /** mất 1 ngày fix cái bug này :(, thêm đoạn code nhỏ giải quyết bao thứ huhu T_T */
//        mViewModel.allPlaylist.observe(viewLifecycleOwner, Observer {
//            Toast.makeText(requireActivity(), "list of playlist seen by home", Toast.LENGTH_LONG)
//                .show()
//        })
    }

    private val observe_hasSync = Observer<Boolean> {

        if (it == false) {

            binding.homeTxtUpdate.visibility = View.VISIBLE
            binding.lottieUpdate.also {  lt ->
                lt.visibility = View.VISIBLE
                lt.playAnimation()
                lt.repeatCount = LottieValueAnimator.INFINITE
            }

            binding.homeProgress.visibility = View.VISIBLE
            mViewModel.count.observe(viewLifecycleOwner, {
                binding.homeProgress.also { mProgress ->
                    mProgress.progress = it
                }
            })
        }else{

            binding.homeTxtUpdate.visibility = View.GONE
            binding.lottieUpdate.also { lt2 ->
                lt2.visibility = View.GONE
                lt2.cancelAnimation()
                lt2.repeatCount = 0
            }

            binding.homeProgress.visibility = View.GONE

        }

    }

    private fun myEyes() {
        mViewModel.hasSynced.observe(viewLifecycleOwner, observe_hasSync)
    }


}