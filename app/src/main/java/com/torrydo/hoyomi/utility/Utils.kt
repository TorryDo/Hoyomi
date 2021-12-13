package com.torrydo.hoyomi.utility

import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources.getSystem
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.torrydo.hoyomi.model.LiveWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class Utils {

    companion object {

        @RequiresApi(Build.VERSION_CODES.M)
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }

        fun saveImgToStorage(myfile: File, bye: ByteArray) {
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(myfile)
                fos.write(bye)
                Log.i("_info", "write image from error success")
            } catch (e: Exception) {
                Log.e("_info", "write image from error ERROR : ${e.printStackTrace()}")
            } finally {
                fos?.close()

            }
        }

        fun saveMediaFileToGallery(
            context: Context,
            videoName: String,
            dirPath: String
        ) {

            var uriSavedVideo: Uri
            var createdvideo: File?
            val resolver: ContentResolver = context.contentResolver


            val videoFileName = "${getAppName(context)}_${System.currentTimeMillis()}.mp4"

            val valuesvideos: ContentValues
            valuesvideos = ContentValues()


            if (Build.VERSION.SDK_INT >= 29) {
                valuesvideos.put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DCIM}/" + getAppName(context)
                )
                valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName)
                valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName)
                valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                valuesvideos.put(
                    MediaStore.Video.Media.DATE_ADDED,
                    System.currentTimeMillis() / 1000
                )
                val collection: Uri =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                uriSavedVideo = resolver.insert(collection, valuesvideos)!!
                /** chú ý */
            } else {
                val directory =
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DCIM + "/" + getAppName(
                        context
                    )
                createdvideo = File(directory, videoFileName)
                valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName)
                valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName)
                valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                valuesvideos.put(
                    MediaStore.Video.Media.DATE_ADDED,
                    System.currentTimeMillis() / 1000
                )
                valuesvideos.put(MediaStore.Video.Media.DATA, createdvideo.absolutePath)
                uriSavedVideo = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    valuesvideos
                )!!
                /** chú ý */
            }


            if (Build.VERSION.SDK_INT >= 29) {
                valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            val pfd: ParcelFileDescriptor

            try {
                pfd = context.contentResolver.openFileDescriptor(uriSavedVideo, "w")!!
                /** chú ý */
                val out = FileOutputStream(pfd.fileDescriptor)

                // get the already saved video as fileinputstream

                // The Directory where your file is saved
                val storageDir: File =
                    File(dirPath)

                //Directory and the name of your video file to copy
                val videoFile = File(dirPath, videoName)
                val `in` = FileInputStream(videoFile)
                val buf = ByteArray(8192)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                out.close()
                `in`.close()
                pfd.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            if (Build.VERSION.SDK_INT >= 29) {
                valuesvideos.clear()
                valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(uriSavedVideo, valuesvideos, null, null)
            }

            /** delete folder */
            File(dirPath).deleteRecursively()
        }


        fun setWhiteStatusTextColor(yn: Boolean, activity: Activity) {
            if (yn) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    /** chưa check */
                    activity.window.insetsController?.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val decorView: View = activity.window.decorView
                        var systemUiVisibilityFlags = decorView.systemUiVisibility
                        systemUiVisibilityFlags =
                            systemUiVisibilityFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                        decorView.systemUiVisibility = systemUiVisibilityFlags

                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    /** chưa check */
                    activity.window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        activity.window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
            }

        }

        fun retrieveVideoFrameFromVideo(videoPath: String?): Bitmap? {
            var bitmap: Bitmap? = null
            var mediaMetadataRetriever: MediaMetadataRetriever? = null
            try {
                mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(videoPath, HashMap<String, String>())
                bitmap = mediaMetadataRetriever.frameAtTime
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mediaMetadataRetriever?.release()
            }
            return bitmap
        }

        fun getAppName(context: Context): String =
            context.applicationInfo.loadLabel(context.packageManager).toString()

        fun setBitmapWallpaper(context: Context?, bm: Bitmap) {
            val wallpaperManager = WallpaperManager.getInstance(context?.applicationContext)
            GlobalScope.launch(Dispatchers.IO) {
                wallpaperManager.setBitmap(bm)
            }
        }

        fun setBitmapLockWallpaper(context: Context?, bitmap: Bitmap) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            GlobalScope.launch(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
            }
        }

        fun hideSystemUI(activity: Activity) {

//        this.requestWindowFeature()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.setDecorFitsSystemWindows(false)
                val controller = activity.window.insetsController
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // All below using to hide navigation bar
                val currentApiVersion = Build.VERSION.SDK_INT
                val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

                // This work only for android 4.4+
                if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
                    activity.window.decorView.systemUiVisibility = flags
                    // Code below is to handle presses of Volume up or Volume down.
                    // Without this, after pressing volume buttons, the navigation bar will
                    // show up and won't hide
                    val decorView = activity.window.decorView
                    decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            decorView.systemUiVisibility = flags
                        }
                    }
                }
            }
        }

        private var gson = Gson()
        fun getGsonInstance(): Gson {
            if (gson == null) {
                gson = GsonBuilder().create()

            }
            return gson
        }

        fun fromPlaylist_to_String(value: Playlist): String {
            val type = object : TypeToken<Playlist>() {}.type
            return Gson().toJson(value, type)
        }

        fun fromString_to_Playlist(value: String): Playlist {
            val type = object : TypeToken<Playlist>() {}.type
            return Gson().fromJson(value, type)
        }

        fun getCurrentTime(): String {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance() //or use getDateInstance()
            val formatedDate = formatter.format(date)

            return formatedDate
        }

        fun shakingBaseUrl(string:String) : String{
            return string
        }

        fun randomArrStaggredFeedItems(mArrs: ArrayList<staggredFeedItems>): ArrayList<staggredFeedItems> {

            val newArr = ArrayList<staggredFeedItems>()

            if (!mArrs.isNullOrEmpty()) {

                for (i in 0..mArrs.size - 1) {
                    if (mArrs.size > 1) {
                        val deathNum = Random.nextInt(0, mArrs.size - 1)
                        newArr.add(mArrs[deathNum])
                        mArrs.removeAt(deathNum)
//                        Log.d("response", "death num : ${deathNum} - mArrs size ${mArrs.size}")
                    } else {
                        newArr.add(mArrs[0])
                        mArrs.removeAt(0)
//                        Log.d("response", "finished - mArrs size ${mArrs.size}")
                        break
                    }
                }
            }
//            Log.e("response","newArr size = ${newArr.size}")
            return newArr
        }

        fun randomArrLiveWallpaper(arr: ArrayList<LiveWallpaper>): ArrayList<LiveWallpaper> {
            val newArr = ArrayList<LiveWallpaper>()
//            GlobalScope.launch() {
            if (arr.isNotEmpty()) {

                for (i in 0..arr.size - 1) {
                    if (arr.size > 1) {
                        val deathNum = Random.nextInt(0, arr.size - 1)
                        newArr.add(arr[deathNum])
                        arr.removeAt(deathNum)
//                                Log.d("response", "death num : ${deathNum} - arr size ${arr.size}")
                    } else {
                        newArr.add(arr[0])
                        arr.removeAt(0)
//                                Log.d("response", "finished - arr size ${arr.size}")
                        break
                    }
//                        }

                }

            }
            return newArr
        }

        fun randomArrString(arr: ArrayList<String>): ArrayList<String> {

            val newArr = ArrayList<String>()
//            GlobalScope.launch() {
                    if (!arr.isNullOrEmpty()) {

                        for (i in 0..arr.size - 1) {
                            if (arr.size > 1) {
                                val deathNum = Random.nextInt(0, arr.size - 1)
                                newArr.add(arr[deathNum])
                                arr.removeAt(deathNum)
//                                Log.d("response", "death num : ${deathNum} - arr size ${arr.size}")
                            } else {
                                newArr.add(arr[0])
                                arr.removeAt(0)
//                                Log.d("response", "finished - arr size ${arr.size}")
                                break
                            }
//                        }

                }

            }
            return newArr
        }


        fun getDeviceWidth_and_Height(activity: Activity, num: Int): Int {
            var returnNum = 0

            val outMetrics = DisplayMetrics()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = activity.display
                display?.getRealMetrics(outMetrics)

                if (num == 0) {
                    returnNum = outMetrics.widthPixels
                } else {
                    returnNum = outMetrics.heightPixels
                }

            } else {
                @Suppress("DEPRECATION")
                val display = activity.windowManager.defaultDisplay
                @Suppress("DEPRECATION")
                display.getRealMetrics(outMetrics)

                if (num == 0) {
                    returnNum = outMetrics.widthPixels
                } else {
                    returnNum = outMetrics.heightPixels
                }

            }
            Log.i(
                "screenInfo",
                "width = ${outMetrics.widthPixels} & height = ${outMetrics.heightPixels}"
            )

            return returnNum

        }

        fun shakeIt(string: String) : String{
            return string
        }

//        fun getItemRatio(itemWidth:Int,ImageWidth:Int){
//        }

        fun getMyPx(dp: Int): Int {
            return (dp * getSystem().displayMetrics.density).toInt()
        }

        fun getMyDp(px: Int): Int {
            return (px / getSystem().displayMetrics.density).toInt()
        }


//        fun focusViewIntro(context: Context, view: View, contentText: String, title: String) {
//            VSpotView.Builder(context)
//                .setTitle(title)
//                .setContentText(contentText)
//                .setGravity(VSpotView.Gravity.auto) //optional
//                .setDismissType(VSpotView.DismissType.outside) //optional - default dismissable by TargetView
//                .setTargetView(view)
//                .setContentTextSize(12)//optional
//                .setTitleTextSize(14)//optional
//                .build()
//                .show()
//        }
    }

}