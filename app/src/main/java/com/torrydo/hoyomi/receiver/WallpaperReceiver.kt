package com.torrydo.hoyomi.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.localData.preferenceDataStore.myPreferenceDataStore
import com.torrydo.hoyomi.localData.roomDB.myAlbumDB
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File


class WallpaperReceiver : BroadcastReceiver() {

    private val mArrs = ArrayList<staggredFeedItems>()
    private var apply: CharSequence = "Both"
    private var hasNoti = false

    override fun onReceive(p0: Context?, p1: Intent?) {

        if (p0 != null && p1 != null) {

            GlobalScope.launch {
                myPreferenceDataStore(p0).getCurrentImagePosition.collect {
                    Log.e("_info", "current image = $it")

                    val bundle = p1.getBundleExtra(CONSTANT.BUNDLE_NAME)
                    if (bundle != null) {

                        hasNoti = bundle.getBoolean(CONSTANT.MNOTI_KEY)

                        apply = bundle.getCharSequence(CONSTANT.MAPPLY, "Both")
                        Log.i("XXX", apply.toString())

                        val saveto = bundle.getBoolean(CONSTANT.SAVE_TO, false)
                        val currentPlaylistID = bundle.getInt(CONSTANT.MYID, 0)

                        if (saveto) {
                            readImageFolder(p0, it)
                            Log.i("_info", "receiver: saveto = true is running")
                        } else {
                            setWallpaperUsingData(p0, currentPlaylistID, it)
                        }
                    }
                }
            }
        } else {
            Log.e("BUGLOL", "receiver : context or intent == null")
        }
    }

    fun setWallpaperUsingData(p0: Context, _currentPlaylistID: Int, currentImg: Int) {
        val mPref = myPreferenceDataStore(p0)
        var slink = CONSTANT.DEFAULT_IMG
        var currentPlaylistID = 0
        currentPlaylistID = _currentPlaylistID

        try {
            GlobalScope.launch(Dispatchers.IO) {

                val plDao = myAlbumDB.getAlbumDB(p0).playlistDao()
                val playlist = plDao.readOnePlaylist(currentPlaylistID)
                Log.d("_info", "TRY : playlist = ${playlist}")

                playlist.list?.forEach {
                    mArrs.add(it)
                }
                Log.d("_info", "TRY : array size = ${mArrs.size}")

            }
        } catch (e: Exception) {
            Log.e("_info", "receiver : ${e.printStackTrace()}")
        } finally {
            GlobalScope.launch(Dispatchers.IO) {
                delay(1500)
                if (currentImg <= mArrs.size - 1) {

                    Log.d("_info", "FINAL : compare = $currentImg  & ${mArrs.size}")

                    val x = mArrs[currentImg].image
                    Log.e("_info", "FINAL : arraySTAG = $x")

                    if (x.resolutions?.get(CONSTANT.resolution_of_StaggredItem + 1)?.url != null) {
                        slink =
                            x.resolutions[CONSTANT.resolution_of_StaggredItem + 1].url.replace(
                                "amp;",
                                ""
                            )
                                .toString()
                        Log.i("_info", "url != null")

                    } else {
                        slink = x.source.url.toString()
                    }

                    val bm = Glide.with(p0).asBitmap().load(slink)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .submit().get()
                    when (apply) {
                        CONSTANT.BOTH_SCREEN -> {
                            Utils.setBitmapWallpaper(p0, bm)
                            Utils.setBitmapLockWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                        CONSTANT.HOME_SCREEN -> {
                            Utils.setBitmapWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                        CONSTANT.LOCK_SCREEN -> {
                            Utils.setBitmapLockWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                        else -> {
                            Utils.setBitmapWallpaper(p0, bm)
                            Utils.setBitmapLockWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                    }

                    Log.i("_info", "FINAL : currentImg1.5 = ${currentImg}")
                    val newC = currentImg + 1
                    mPref.setCurrentImagePosition(newC)
                    Log.i("_info", "FINAL : newC = ${newC}")
                } else {
                    cancelReceiver(p0)
                    Log.i("_info", "FINAL : cancel receiver")
                }
            }
        }
    }

    fun readImageFolder(p0: Context, currentImg: Int) {
        val filename = CONSTANT.FILE_NAME
        val myExternalFile = File(p0.getExternalFilesDir(filename), "")

        var mArrs = ArrayList<String>()
        myExternalFile.walk().forEach { f ->

            if (f.isFile) {
                mArrs.add(f.absolutePath.toString())
                Log.i("_info", "readfile: " + f.name.toString())
            }
        }
        if (mArrs.isNotEmpty()) {
            val mPref2 = myPreferenceDataStore(p0)

            GlobalScope.launch(Dispatchers.IO) {
                Log.i("_info", "receiver: mArrs is not empty --->> current image is : $currentImg")
                if (currentImg <= mArrs.size - 1) {
                    val bm = BitmapFactory.decodeFile(mArrs[currentImg])

                    when (apply) {
                        CONSTANT.BOTH_SCREEN -> {
                            Utils.setBitmapWallpaper(p0, bm)
                            Utils.setBitmapLockWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                        CONSTANT.HOME_SCREEN -> {
                            Utils.setBitmapWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                        CONSTANT.LOCK_SCREEN -> {
                            Utils.setBitmapLockWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                        else -> {
                            Utils.setBitmapWallpaper(p0, bm)
                            Utils.setBitmapLockWallpaper(p0, bm)
                            if (hasNoti) {
                                setNotification(p0)
                            }
                        }
                    }

                    mPref2.setCurrentImagePosition(currentImg + 1)
                } else {
                    Log.i("_info", "receiver : currentImage > mArrs.size-1")
                    cancelReceiver(p0)
                }
            }


        } else {
            Log.e("BUGLOL", "image folder is empty")
        }
    }

    fun setNotification(p0: Context?) {
        val notiManager = NotificationManagerCompat.from(p0!!)
        val notiChannel =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel("myID", "Wallpapers", NotificationManager.IMPORTANCE_DEFAULT)
            } else {
                TODO("VERSION.SDK_INT < O")
            }

        notiManager.createNotificationChannel(notiChannel)

        val builder = NotificationCompat.Builder(p0, "myID")
            .setContentTitle("Success")
            .setContentText("Have a nice day <3")
            .setSmallIcon(R.mipmap.hym_icon)
        notiManager.notify(1, builder.build())
    }

    fun cancelReceiver(context: Context) {
        val intent = Intent(context, WallpaperReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_NO_CREATE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } else {
            Log.e("_info", "lỗi éo biết. alarmManager = null")
        }
    }
}


