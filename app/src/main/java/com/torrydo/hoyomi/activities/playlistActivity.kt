package com.torrydo.hoyomi.activities

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.adapter.playlist_recyclerAdapter
import com.torrydo.hoyomi.databinding.ActivityPlaylistBinding
import com.torrydo.hoyomi.interfaces.clickerInterface
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import com.torrydo.hoyomi.viewModel.myViewmodelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

class playlistActivity : AppCompatActivity() {

    private val mTag = "fromPlaylist"
    private var position = 0

    private var playList: Playlist? = null
    private lateinit var binding: ActivityPlaylistBinding

    private lateinit var mViewmodel: myViewModel
    private var mAdapter: playlist_recyclerAdapter? = null

    private val mContext = WeakReference<Context>(this).get()!!

    private val mArrs = ArrayList<staggredFeedItems>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        mViewmodel =
            ViewModelProvider(this, myViewmodelFactory(application))
                .get(myViewModel::class.java)


        val intent = intent
        if (intent != null) {
            position = intent.getIntExtra("lib_RecyclerApapter_in_library", 0)
//                playList = Utils.fromString_to_Playlist(tempStr!!)


            binding.playlistRecyclerView.also {
                mAdapter = playlist_recyclerAdapter(
                    this,
                    mArrs,
                    Utils.getDeviceWidth_and_Height(this, 0),
                    Utils.getDeviceWidth_and_Height(this, 1),
                    object : clickerInterface {
                        override fun click(position: Int) {

//                                val newArr = tempArr.filterIndexed{index, staggredFeedItems -> index != position }
                            mArrs.removeAt(position)
//                                if(x){
//                                    Log.i("_info","removed")
//                                }

                            mViewmodel.updatePlaylist(
                                Playlist(
                                    playList!!.id,
                                    playList!!.playlistTitle,
                                    playList!!.date,
                                    mArrs,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            )

//                                it.removeViewAt(position)
                            mAdapter!!.notifyItemRemoved(position)
                            mAdapter!!.notifyItemRangeChanged(position, mArrs.size)
                            mAdapter!!.notifyDataSetChanged()
                        }
                    }
                )
                it.setHasFixedSize(true)
                it.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                it.adapter = mAdapter
            }

            mViewmodel.allPlaylist.observe(this, { pl ->
                if (pl[position].list != null) {

                    playList = pl[position]

                    if (mArrs.isNotEmpty()) {
                        mArrs.clear()
                    }

                    binding.playlistCustomToolbar.customToolbarTxtTitle.text =
                        pl[position].playlistTitle

                    playList!!.list!!.forEach { v ->
                        mArrs.add(v)
                    }
                    mAdapter?.notifyDataSetChanged()
                }
            })

        }
        defaulStuffs()
        clickTriggers()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            mAdapter = null
            binding.playlistRecyclerView.adapter = null
        }
    }

    private fun defaulStuffs() {
        binding.playlistCustomToolbar.customToolbarMenu.setOnClickListener {
            var popMenu : PopupMenu? = PopupMenu(mContext, binding.playlistCustomToolbar.customToolbarMenu)
            val menu = popMenu!!.menu
            menu.add(0, 0, 0, getString(R.string.menu_edit))
            popMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.groupId == 0) {
                    var alertDialog: AlertDialog? =
                        MaterialAlertDialogBuilder(mContext, R.style.alertDialog_roundCorner)
                            .setView(R.layout.playlist_creator).create()
                    alertDialog!!.show()

                    var title = alertDialog.findViewById<TextView>(R.id.creator_txt)
                    var editText = alertDialog.findViewById<EditText>(R.id.creator_edittext)
                    var btNegative = alertDialog.findViewById<TextView>(R.id.creator_txtCancel)
                    var btPositive =
                        alertDialog.findViewById<MaterialButton>(R.id.creator_buttonCreate)

                    title?.text = getString(R.string.dialog_rename_title)
                    editText?.hint =
                        "the previous name was '${binding.playlistCustomToolbar.customToolbarTxtTitle.text}'"
                    btNegative?.text = getString(R.string.dialog_rename_button_negative)
                    btPositive?.text = getString(R.string.dialog_rename_button_positive)

                    alertDialog.setOnCancelListener {
                        alertDialog = null
                        title = null
                        editText = null
                        btNegative = null
                        btPositive = null
                    }

                    btNegative?.setOnClickListener {
                        alertDialog?.dismiss()

                        alertDialog = null
                        title = null
                        editText = null
                        btNegative = null
                        btPositive = null

                    }

                    btPositive?.setOnClickListener {

                        val playlist = Playlist(
                            playList!!.id,
                            editText!!.text.toString(),  // edit playlist title
                            playList!!.date,
                            mArrs,
                            playList!!.lastModifided,
                            playList!!.dailyTime,
                            playList!!.hasNoti,
                            playList!!.order,
                            playList!!.apply
                        )

                        mViewmodel.updatePlaylist(playlist)

                        alertDialog?.dismiss()

                        alertDialog = null
                        title = null
                        editText = null
                        btNegative = null
                        btPositive = null
                    }

                }
                return@setOnMenuItemClickListener false
            }

            popMenu.setOnDismissListener {
                popMenu = null
            }
            popMenu?.show()

        }
    }

    private fun clickTriggers() {
        binding.playlistCustomToolbar.customToolbarIconBack.setOnClickListener {
            this.finish()
        }
    }
}