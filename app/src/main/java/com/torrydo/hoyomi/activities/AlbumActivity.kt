package com.torrydo.hoyomi.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.adapter.adapter_album
import com.torrydo.hoyomi.databinding.ActivityAlbumBinding
import com.torrydo.hoyomi.interfaces.playlist_interface
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.viewModel.myViewModel
import com.torrydo.hoyomi.viewModel.myViewmodelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference

class AlbumActivity : AppCompatActivity() {

    private val mytag = "fromAlbum"

    private var binding: ActivityAlbumBinding? = null
    private lateinit var mViewModel: myViewModel

    private val mContext = WeakReference<Context>(this).get()!!

    private val mArrs = ArrayList<Playlist>()
    private lateinit var adapter: adapter_album

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(LayoutInflater.from(this))
        setContentView(binding?.root)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        mViewModel =
            ViewModelProvider(this, myViewmodelFactory(application)).get(myViewModel::class.java)

        binding!!.albumCustomToolbar.customToolbarTxtTitle.text = getString(R.string.album_title)

        binding?.albumRecyclerView.also {
            val deviceWidth = Utils.getDeviceWidth_and_Height(this, 0)
            adapter = adapter_album(
                this,
                mArrs,
                2,
                deviceWidth,
                object : playlist_interface {
                    override fun onItemLongCLick(position: Int) {
                        try {

                            var alertDialog: AlertDialog? = MaterialAlertDialogBuilder(
                                mContext,
                                R.style.alertDialog_roundCorner
                            )
                                .setView(R.layout.dialog_alert)
                                .create()
                            alertDialog!!.show()

                            var title: TextView? =
                                alertDialog.findViewById<TextView>(R.id.alertTitle)
                            var context: TextView? =
                                alertDialog.findViewById<TextView>(R.id.alertContent)
                            var btNegative: Button? =
                                alertDialog.findViewById<Button>(R.id.alertButtonNegative)
                            var btPositive: Button? =
                                alertDialog.findViewById<Button>(R.id.alertButtonPositive)

                            title!!.text = getString(R.string.question_delete_playlist)
                            context!!.text = getString(R.string.question_warn_deleteplaylist)
                            btNegative!!.text = getString(R.string.button_negative_cancel)
                            btPositive!!.text = getString(R.string.button_positive_ok)

                            alertDialog.setOnCancelListener {
                                title = null
                                context = null
                                btNegative = null
                                btPositive = null
                            }

                            btPositive?.setOnClickListener {
                                mViewModel.deletePlaylist(mArrs[position])
                                adapter.notifyDataSetChanged()

                                alertDialog.dismiss()

                                title = null
                                context = null
                                btNegative = null
                                btPositive = null

                                MotionToast.createColorToast(mContext as AlbumActivity,
                                    "SUCCESS",
                                    "Deleted successfully",
                                    MotionToast.TOAST_SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    null
                                )

                            }

                            btNegative?.setOnClickListener {
                                alertDialog.dismiss()

                                title = null
                                context = null
                                btNegative = null
                                btPositive = null
                            }


                        } catch (e: Exception) {
                            Log.e(mytag, "delete not or smth i dunno")
                        }

                    }
                })
            it?.setHasFixedSize(true)
            it?.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
            it?.adapter = adapter
        }

        clickTrigger()
    }


    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }


    fun clickTrigger() {

        binding!!.albumCustomToolbar.customToolbarMenu.setOnClickListener {
            val popupMenu = PopupMenu(mContext, binding!!.albumCustomToolbar.customToolbarMenu)
            val menu = popupMenu.menu
            menu.add(0, 0, 0, getString(R.string.howto_delete_playlist))
            popupMenu.setOnMenuItemClickListener {
                if (it.groupId == 0) {

                    MotionToast.createColorToast(mContext as AlbumActivity,
                        "INFO",
                        getString(R.string.info_delete_playlist),
                        MotionToast.TOAST_INFO,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        null
                    )

                }
                return@setOnMenuItemClickListener true
            }

            popupMenu.show()

        }
        binding!!.albumCustomToolbar.customToolbarIconBack.setOnClickListener {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.allPlaylist.observe(this, { listPL ->
            lifecycleScope.launch(Dispatchers.IO) {
                mArrs.clear()
                listPL.forEach { pl ->
                    mArrs.add(pl)
                }
            }
            adapter.notifyDataSetChanged()
        })
    }
}