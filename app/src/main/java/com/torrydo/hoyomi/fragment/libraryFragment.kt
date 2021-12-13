package com.torrydo.hoyomi.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.AlbumActivity
import com.torrydo.hoyomi.activities.AutoSetActivity
import com.torrydo.hoyomi.activities.MainActivity
import com.torrydo.hoyomi.adapter.adapter_album
import com.torrydo.hoyomi.adapter.lib_RecyclerAdapter_2_in_library
import com.torrydo.hoyomi.databinding.FragmentLibraryBinding
import com.torrydo.hoyomi.interfaces.playlist_interface
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.torrydo.hoyomi.activities.pictureActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference

class libraryFragment : Fragment() {

    private var binding: FragmentLibraryBinding? = null
    private val mViewModel: myViewModel by activityViewModels()

    private val mContext get() = WeakReference<Context>(requireContext()).get()!!
    private val mActivity get() = WeakReference<Activity>(requireActivity()).get()!!

    private var adapter: adapter_album? = null
    private val tempArrs = ArrayList<Playlist>()

    val ObserverAllPlaylist = Observer<List<Playlist>> { listPL ->
        lifecycleScope.launch(Dispatchers.IO) {
            if (tempArrs.isNotEmpty()) {
                tempArrs.clear()
            }
            listPL.forEach { pl ->
                tempArrs.add(pl)
            }

        }
        adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)

        binding!!.libRecyclerView.also {
            val deviceWidth = Utils.getDeviceWidth_and_Height(mActivity, 0)

            adapter = adapter_album(mContext, tempArrs, 1, deviceWidth,
                object : playlist_interface {
                    override fun onItemLongCLick(position: Int) {
                        Log.i("_info", "position = $position")
                    }
                }
            )
            it.adapter = adapter
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter?.notifyDataSetChanged()

            onclickFunction()
            myEyes()

            /**
             * bug : can not scroll recyclerview inside viewpager because of onclicklistener
             *
             * bằng cách loại bỏ lướt ở viewpager, ta đã fix được cái lỗi này :((, bye bye bug l**  p(>_<)q
             *
             * update : bằng trí tưởng tượng thiên cmn tài t đã perfectly fix đc cái bug l** này mà ko phải loại bỏ HorizontalScroll,
             *          mặc dù đ biết có hậu quả ko nhưng kệ đi, fix được là ngon ròi :)))
             * */
            it.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> (mContext as MainActivity).binding!!.mainViewPager2.isUserInputEnabled =
                            false
                        MotionEvent.ACTION_UP -> (mContext as MainActivity).binding!!.mainViewPager2.isUserInputEnabled =
                            true
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                    Log.d("libRecyclerView", "dissalow intercept")
                }
            })
        }

        binding!!.libRecyclerView2.also {
            val tempLink = arrayListOf<Int>(
                R.drawable.nar_sas_1,
                R.drawable.op_1,
                R.drawable.aot_1,
                R.drawable.sao_1,
                R.drawable.nar_sas_2,
                R.drawable.op_2,
                R.drawable.aot_2,
                R.drawable.sao_2
            )
            it.setHasFixedSize(true)
            it.layoutManager =
                LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            it.adapter = lib_RecyclerAdapter_2_in_library(mContext, tempLink)
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mActivity.isFinishing) {
            adapter = null
            binding!!.libRecyclerView.adapter = null
            binding!!.libRecyclerView2.adapter = null
            binding!!.root.removeAllViews()
            binding = null
        }
    }


    private fun myEyes() {
        mViewModel.allPlaylist.observe(viewLifecycleOwner, ObserverAllPlaylist)
    }

    private fun onclickFunction() {

//        binding!!.libView2Top.setOnClickListener {
//            val i = Intent(mContext, MusicActivity::class.java)
//            startActivity(i)
//        }

        binding!!.libView4Top.setOnClickListener {
            MotionToast.createColorToast(
                mActivity,
                "WARNING",
                getString(R.string.sorry_update_later),
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                null
            )
        }

        binding!!.libView1Top.setOnClickListener {
            val i = Intent(mContext, AutoSetActivity::class.java)
            startActivity(i)
        }

        binding!!.libTxtViewAll.setOnClickListener {
            val i = Intent(mContext, AlbumActivity::class.java)
            startActivity(i)
        }

        binding!!.libTxtCreate.setOnClickListener {
            val alertDialog =
                MaterialAlertDialogBuilder(mContext, R.style.alertDialog_roundCorner)
                    .setView(R.layout.playlist_creator).create()
            alertDialog.show()

            val itemCancel = alertDialog.findViewById<TextView>(R.id.creator_txtCancel)
            val itemCreate = alertDialog.findViewById<MaterialButton>(R.id.creator_buttonCreate)
            val itemEdittext = alertDialog.findViewById<EditText>(R.id.creator_edittext)

            itemCancel?.setOnClickListener {
                alertDialog.dismiss()
            }
            itemCreate?.setOnClickListener {

                val tempTitle = itemEdittext?.text.toString()

                if (!tempTitle.isEmpty() && tempTitle.length < 25) {

                    val stagList = ArrayList<staggredFeedItems>()

                    mViewModel.addPlaylist(
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

                    adapter?.notifyDataSetChanged()

                    MotionToast.createColorToast(
                        mActivity,
                        "SUCCESS",
                        "$tempTitle created",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        null
                    )

                    alertDialog.dismiss()
                } else {
                    MotionToast.createColorToast(
                        mActivity,
                        "ERROR",
                        getString(R.string.cant_blank_of_exceed_25chars),
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        null
                    )
                }
            }

        }
    }
}