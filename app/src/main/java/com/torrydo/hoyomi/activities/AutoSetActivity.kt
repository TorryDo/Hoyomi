package com.torrydo.hoyomi.activities

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.adapter.adapter_autoWallpaper
import com.torrydo.hoyomi.databinding.ActivityAutosetBinding
import com.torrydo.hoyomi.databinding.ItemPlaylistBinding
import com.torrydo.hoyomi.interfaces.RecyclerInterface
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.receiver.WallpaperReceiver
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth
import com.torrydo.hoyomi.viewModel.myViewModel
import com.torrydo.hoyomi.viewModel.myViewmodelFactory
import www.sanju.motiontoast.MotionToast
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class AutoSetActivity : AppCompatActivity() {

    private var binding: ActivityAutosetBinding? = null
    private var mViewModel: myViewModel? = null

    private val mContext = WeakReference<Context>(this).get()!!
    private val mActivity = WeakReference<AutoSetActivity>(this).get()!!

    private var mArrs = ArrayList<Playlist>()
    private lateinit var stagItem: staggredFeedItems
    private var mAdapter: adapter_autoWallpaper? = null

    private var myId = 0

    private val playlist_Observer = Observer<List<Playlist>> { value ->
        value.forEach {
            mArrs.add(it)
        }

        binding!!.autosetRecyclerView.also {
            it.setHasFixedSize(true)
            mAdapter = adapter_autoWallpaper(this, mArrs, object : RecyclerInterface {
                override fun onItemCLick(position: Int, itemBinding: ItemPlaylistBinding) {
                    if (mArrs[position].list!!.size > 3) {

                        binding!!.autosetIncludePlaylist.itemAutosetButtonSet.also {
                            it.alpha = 1f
                            it.isEnabled = true
                        }

                        var temps = ""
                        if (mArrs[position].list!![0].image.resolutions != null) {
                            temps =
                                mArrs[position].list!![0].image.resolutions!![2].url.replace(
                                    "amp;",
                                    ""
                                )
                        } else {
                            val _temps = mArrs[position].list!![0].image.source.url
                            temps = convertSmth.HightReso_to_LowReso(_temps)
                        }
                        stagItem = mArrs[position].list!!.get(0)

                        Glide.with(this@AutoSetActivity).load(temps)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(binding!!.autosetIncludePlaylist.itemAvatar.itemLibImageView)

                        /** set title */
                        var playlistTitle = getString(R.string.app_name)
                        playlistTitle = mArrs[position].playlistTitle
                        binding!!.autosetIncludePlaylist.title.text = playlistTitle

                        /** display number of image */
                        var imageNumber = "null"
                        imageNumber = mArrs[position].list?.size.toString()
                        binding!!.autosetIncludePlaylist.itemAvatar.itemLibTxtTitle.also {
                            it.text = getString(R.string.text_image_number) +" : " + imageNumber
                            it.setTextColor(
                                ContextCompat.getColor(
                                    this@AutoSetActivity,
                                    R.color.dark_gray
                                )
                            )
                        }

                        val x = mArrs[position]
                        myId = x.id

                    } else {

                        MotionToast.createColorToast(mContext as AutoSetActivity,
                            "ERROR",
                            getString(R.string.text_image_higher_than_3),
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            null
                        )

                    }
                }

                override fun onItemLongCLick(position: Int, binding: ItemPlaylistBinding) {}

            })
            it.adapter = mAdapter
            it.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
            mAdapter?.notifyDataSetChanged()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutosetBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        mViewModel = ViewModelProvider(this, myViewmodelFactory(application))
            .get(myViewModel::class.java)
        myEyes()
        modifyDefaultStuffs()

//        Snackbar.make(mContext,binding!!.root,getString(R.string.feature_not_stable),7000).show()

        val x = binding!!.autosetIncludePlaylist
        x.itemAvatar.itemLibImageView.also {
            it.requestLayout()
            it.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            it.layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        }

        customIncludeTags()
        clickTriggers()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {

            mViewModel = null

            mAdapter = null
            binding?.autosetRecyclerView?.adapter = null
            binding?.root?.removeAllViews()
            binding = null
        }
    }

    private fun modifyDefaultStuffs() {
        // đổi khung item auto
        binding!!.autosetIncludePlaylist.constraintAutoset.also {
            it.requestLayout()
            it.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            it.layoutParams.height = (Utils.getDeviceWidth_and_Height(mActivity,0).toLong() * 0.9).toInt()
        }

        binding!!.autosetIncludePlaylist.itemAvatar.itemLibImageView.also {
            it.setImageDrawable(
                ContextCompat.getDrawable(
                    mContext,
                    R.drawable.gradient_light_pink_blue
                )
            )
        }
        binding!!.autosetIncludePlaylist.itemAutosetReuse1.txtOption.text = CONSTANT.DAY_1
        binding!!.autosetIncludePlaylist.itemAutosetReuse2.switchRight.isChecked = true
//        binding.autosetIncludePlaylist.itemAutosetReuse3.txtOption.text = "Ngẫu nhiên"
        binding!!.autosetIncludePlaylist.itemAutosetReuse4.txtOption.text = CONSTANT.BOTH_SCREEN

        binding!!.autosetIncludePlaylist.itemAutosetButtonSet.also {
            it.isClickable = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val writeEx = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (checkCallingOrSelfPermission(writeEx) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(writeEx), 1)
            }
            val ignoreBattery = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            if (checkCallingOrSelfPermission(ignoreBattery) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(ignoreBattery), 2)
            }

            /** chạy trong nền */
            val intent = Intent()
            val packN = getString(R.string.package_name)
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packN)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packN")
                startActivity(intent)
            }
        }

        val b = PendingIntent.getBroadcast(
            this@AutoSetActivity,
            100,
            Intent(this, WallpaperReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE
        ) != null
        if (b) {
            binding!!.autosetIncludePlaylist.itemAutosetButtonSet.visibility = View.GONE
            binding!!.autosetIncludePlaylist.itemAutosetButtonSetCancel.visibility = View.VISIBLE
        } else {
            binding!!.autosetIncludePlaylist.itemAutosetButtonSet.visibility = View.VISIBLE
            binding!!.autosetIncludePlaylist.itemAutosetButtonSetCancel.visibility = View.GONE
        }
    }

    private fun myEyes() {
        mViewModel!!.allPlaylist.observe(this, playlist_Observer)
    }

    private fun clickTriggers() {

        binding!!.autosetToolbar.customToolbarMenu.setOnClickListener {
            var popupMenu : PopupMenu? = PopupMenu(mContext,it)
            var menu : Menu? = popupMenu!!.menu
            menu!!.add(0,0,0,getString(R.string.report_error))

            popupMenu.setOnMenuItemClickListener {
                if(it.groupId == 0){

                    popupMenu?.dismiss()
                    popupMenu = null
                    menu = null

                    val addresses = arrayOf(getString(R.string.dev_email))
                    val subject = getString(R.string.email_title_report_error)
                    val attachment: Uri? = null

                    val intent0 = Intent(Intent.ACTION_SEND);

                    intent0.data = Uri.parse("mailto:");
                    intent0.type = "message/rfc822";
0
//                            intent0.type = "*/*";
                    intent0.putExtra(Intent.EXTRA_EMAIL, addresses);
                    intent0.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent0.putExtra(Intent.EXTRA_STREAM, attachment);
                    if (intent0.resolveActivity(mContext.packageManager) != null) {
                        startActivity(Intent.createChooser(intent0, "send Email"));
                    }
                }
                return@setOnMenuItemClickListener false
            }

            popupMenu?.setOnDismissListener {
                popupMenu = null
                menu = null
            }

            popupMenu?.show()

        }

        binding!!.autosetToolbar.customToolbarIconBack.setOnClickListener {
            super.onBackPressed()
        }

        binding!!.autosetIncludePlaylist.itemAutosetButtonSetCancel.setOnClickListener {

            mViewModel?.clearImageFolder(CONSTANT.FILE_NAME)

            val intent = Intent(this, WallpaperReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this@AutoSetActivity,
                100,
                intent,
                PendingIntent.FLAG_NO_CREATE
            )
            val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            if (pendingIntent != null /*&& alarmManager != null*/) {
                alarmManager!!.cancel(pendingIntent)
                pendingIntent.cancel()

                mViewModel!!.clearImageFolder(CONSTANT.FILE_NAME)

                MotionToast.createColorToast(mContext as AutoSetActivity,
                    "INFO",
                    getString(R.string.cancel_repetition),
                    MotionToast.TOAST_INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    null
                )
                binding!!.autosetIncludePlaylist.itemAutosetButtonSet.also {
                    it.visibility = View.VISIBLE
                    it.isEnabled = false
                    it.alpha = 0.4f
                }
                binding!!.autosetIncludePlaylist.itemAutosetButtonSetCancel.visibility = View.GONE

            } else {
                MotionToast.createColorToast(mContext as AutoSetActivity,
                    "WARNING",
                    getString(R.string.no_cancel_repetition),
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    null
                )
                binding!!.autosetIncludePlaylist.itemAutosetButtonSet.also {
                    it.visibility = View.VISIBLE
                    it.isEnabled = false
                    it.alpha = 0.4f
                }
                binding!!.autosetIncludePlaylist.itemAutosetButtonSetCancel.visibility = View.GONE
            }

        }
        binding!!.autosetIncludePlaylist.itemAutosetButtonSet.setOnClickListener {
            val s = binding!!.autosetIncludePlaylist

            val dailyTime = s.itemAutosetReuse1.txtOption.text
            val hasNoti = s.itemAutosetReuse2.switchRight.isChecked
            val order = s.itemAutosetReuse3.txtOption.text
            val apply = s.itemAutosetReuse4.txtOption.text


            var alertDialog: AlertDialog? =
                MaterialAlertDialogBuilder(this, R.style.alertDialog_roundCorner)
                    .also { dia ->
                        dia.setTitle(getString(R.string.set_wallpaper))
                        dia.setMessage(getString(R.string.info_storage_or_internet))

                        dia.setPositiveButton(
                            getString(R.string.storage)
                        ) { p0, p1 ->
                            mViewModel!!.setImagePosition(0)
                            mViewModel!!.saveFileToExternalStorage(
                                myId,
                                dailyTime,
                                hasNoti,
                                order,
                                apply
                            )

                            alarmTriggers(hasNoti, apply, myId, saveTo = true, dailyTime)

                            binding!!.autosetIncludePlaylist.itemAutosetButtonSet.visibility = View.GONE
                            binding!!.autosetIncludePlaylist.itemAutosetButtonSetCancel.visibility = View.VISIBLE

                            MotionToast.createColorToast(mContext as AutoSetActivity,
                                "SUCCESS",
                                getString(R.string.successful_setup),
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                null
                            )

                        }
                        dia.setNegativeButton(
                            getString(R.string.mobile_network)
                        ) { p0, p1 ->
                            mViewModel!!.setImagePosition(0)

                            alarmTriggers(hasNoti, apply, myId, saveTo = false, dailyTime)

                            binding!!.autosetIncludePlaylist.itemAutosetButtonSet.visibility = View.GONE
                            binding!!.autosetIncludePlaylist.itemAutosetButtonSetCancel.visibility = View.VISIBLE

                            MotionToast.createColorToast(mContext as AutoSetActivity,
                                "SUCCESS",
                                getString(R.string.successful_setup),
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                null
                            )

                        }
                    }.create()
            alertDialog?.setOnCancelListener {
                alertDialog = null
            }
            alertDialog?.show()


        }

        /** reuse 1 */
        binding!!.autosetIncludePlaylist.itemAutosetReuse1.linear.setOnClickListener {

            val popMenu = PopupMenu(this, it)
            val menu = popMenu.menu
            val tempArrs =
                arrayListOf(
//                    CONSTANT.DAY_7,
//                    CONSTANT.DAY_3,
                    CONSTANT.DAY_1,
                    CONSTANT.HOUR_12,
                    CONSTANT.HOUR_1,
                    CONSTANT.MINUTE_30,
                    CONSTANT.MINUTE_5
                )

            tempArrs.forEach { v ->
                menu.add(
                    v/*.replace("days", "ngày")
                        .replace("day", "ngày")
                        .replace("hours", "giờ")
                        .replace("hour", "giờ")
                        .replace("minutes", "phút")*/
                )
            }
            popMenu.show()

            popMenu.setOnMenuItemClickListener {
                binding!!.autosetIncludePlaylist.itemAutosetReuse1.txtOption.text =
                    it.title.toString()

                return@setOnMenuItemClickListener true
            }

        }

        /** reuse 3 */
        binding!!.autosetIncludePlaylist.itemAutosetReuse3.linear.setOnClickListener {
            val popMenu = PopupMenu(this, it)
            val menu = popMenu.menu
            val tempArrs =
                arrayListOf(getString(R.string.order), getString(R.string.random))

            tempArrs.forEach { v ->
                menu.add(
                    v.replace("Order", "Thứ tự").replace("Random", "Ngẫu nhiên")

                )
            }
            popMenu.show()
            popMenu.setOnMenuItemClickListener {
                binding!!.autosetIncludePlaylist.itemAutosetReuse3.txtOption.text =
                    it.title.toString()

                return@setOnMenuItemClickListener true
            }

        }
        /** reuse 4 */
        binding!!.autosetIncludePlaylist.itemAutosetReuse4.linear.setOnClickListener {
            val popMenu = PopupMenu(this, it)
            val menu = popMenu.menu
            val tempArrs =
                arrayListOf(
                    CONSTANT.BOTH_SCREEN,
                    CONSTANT.HOME_SCREEN,
                    CONSTANT.LOCK_SCREEN
                )

            tempArrs.forEach { v ->
                menu.add(
                    v
                )
            }
            popMenu.show()

            popMenu.setOnMenuItemClickListener {
                binding!!.autosetIncludePlaylist.itemAutosetReuse4.txtOption.text =
                    it.title.toString()

                return@setOnMenuItemClickListener true
            }

        }

    }

    private fun alarmTriggers(
        hasNoti: Boolean,
        apply: CharSequence,
        myId: Int,
        saveTo: Boolean,
        dailyTime: CharSequence
    ) {
        val bundle = bundleOf(
            Pair(CONSTANT.MNOTI_KEY, hasNoti),
//                        Pair(CONSTANT.MORDER, order),
            Pair(CONSTANT.MAPPLY, apply),
            Pair(CONSTANT.MYID, myId),
            Pair(CONSTANT.SAVE_TO, saveTo)
        )
        val intent = Intent(
            this@AutoSetActivity,
            WallpaperReceiver::class.java
        ).putExtra(CONSTANT.BUNDLE_NAME, bundle)

        val pendingIntent = PendingIntent.getBroadcast(
            this@AutoSetActivity,
            100,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val alarmManager =
            this@AutoSetActivity.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        val calendarRTC: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }
        when (dailyTime) {
            CONSTANT.DAY_7 -> {
                calendarRTC.set(Calendar.DAY_OF_WEEK, 1)
                alarmManager?.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendarRTC.timeInMillis + 39 * 1000,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }
            CONSTANT.DAY_3 -> {
                calendarRTC.set(Calendar.HOUR_OF_DAY, 1)
                alarmManager?.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendarRTC.timeInMillis + 39 * 1000,
                    AlarmManager.INTERVAL_DAY * 3,
                    pendingIntent
                )
            }
            CONSTANT.DAY_1 -> {
                calendarRTC.set(Calendar.HOUR_OF_DAY, 1)
                alarmManager?.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendarRTC.timeInMillis + 39 * 1000,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }

            CONSTANT.HOUR_12 ->
                alarmManager?.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 39 * 1000,
                    AlarmManager.INTERVAL_HALF_DAY,
                    pendingIntent
                )
            CONSTANT.HOUR_1 ->
                alarmManager?.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 39 * 1000,
                    AlarmManager.INTERVAL_HOUR,
                    pendingIntent
                )
            CONSTANT.MINUTE_30 ->
                alarmManager?.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 39 * 1000,
                    AlarmManager.INTERVAL_HALF_HOUR,
                    pendingIntent
                )
            CONSTANT.MINUTE_5 ->
                alarmManager?.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 39 * 1000,
                    5 * 60 * 1000,
                    pendingIntent
                )

        }
    }

    private fun customIncludeTags() {
        // xóa text dưới playlist
        binding!!.autosetIncludePlaylist.itemAvatar.itemLibTxtTitle.text = ""
        // custom title text
        binding!!.autosetToolbar.customToolbarTxtTitle.text = getString(R.string.autoset_title)


        /** custom txt option 1,2,3 */
        val reuse1 = binding!!.autosetIncludePlaylist.itemAutosetReuse1
        reuse1.txtStart.text = getString(R.string.time)
        reuse1.linear.visibility = View.VISIBLE

        val reuse2 = binding!!.autosetIncludePlaylist.itemAutosetReuse2
        reuse2.txtStart.text = getString(R.string.notification)
        reuse2.switchRight.visibility = View.VISIBLE

//        val reuse3 = binding!!.autosetIncludePlaylist.itemAutosetReuse3
//        reuse3.txtStart.text = "Trình tự"
//        reuse3.linear.visibility = View.VISIBLE

        val reuse4 = binding!!.autosetIncludePlaylist.itemAutosetReuse4
        reuse4.txtStart.text = getString(R.string.apply)
        reuse4.linear.visibility = View.VISIBLE
        /** custom txt option 1,2,3 */
    }

}