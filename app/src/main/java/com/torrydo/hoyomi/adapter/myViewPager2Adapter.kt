package com.torrydo.hoyomi.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class myViewPager2Adapter(fm:FragmentManager,lifecycle:Lifecycle,_ArrFragment:List<Fragment>) : FragmentStateAdapter(fm,lifecycle) {

    private val mArrFragment = _ArrFragment

    override fun getItemCount(): Int {
        return mArrFragment.size
    }

    override fun createFragment(position: Int): Fragment {
        return mArrFragment[position]
    }
}