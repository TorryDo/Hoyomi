package com.torrydo.hoyomi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.databinding.ItemProfileBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.Profile

class adapter_profile(
    val context: Context,
    val mArrs: ArrayList<Profile>,
    val deviceWidth: Int,
    val mInterface: clickerInterface
) : RecyclerView.Adapter<adapter_profile.profileHolder>() {


    class profileHolder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): profileHolder {
        return profileHolder(
            ItemProfileBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: profileHolder, position: Int) {
        val constraint = holder.binding.constraintLayout
        val img = holder.binding.img
        val title = holder.binding.title
        val content = holder.binding.content

        constraint.requestLayout()
        constraint.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        constraint.layoutParams.height = (deviceWidth.toLong() / 1.8).toInt()

        title.text = mArrs[position].title
        content.text = mArrs[position].content

        when (position) {
            0 -> img.setBackgroundColor(ContextCompat.getColor(context, R.color.white_blue))
            1 -> img.setBackgroundColor(ContextCompat.getColor(context, R.color.white_orange))
            2 -> img.setBackgroundColor(ContextCompat.getColor(context, R.color.white_pink))
            3 -> img.setBackgroundColor(ContextCompat.getColor(context, R.color.alert_green_lighter))
            4 -> img.setBackgroundColor(ContextCompat.getColor(context, R.color.white_yellow))
            5 -> img.setBackgroundColor(ContextCompat.getColor(context, R.color.gray))
        }

        clicker(position,holder)
    }

    private fun clicker(position: Int,holder: profileHolder) {
        holder.binding.card.setOnClickListener {
            mInterface.click(position)
        }
    }

    override fun getItemCount(): Int {
        return mArrs.size
    }
}