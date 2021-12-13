package com.torrydo.hoyomi.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.localData.preferenceDataStore.myPreferenceDataStore
import com.torrydo.hoyomi.localData.roomDB.myAlbumDB
import com.torrydo.hoyomi.localData.roomDB.myDAO
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.feedPackage.superComplexFeeds
import com.torrydo.hoyomi.model.staggredFeedItems
import com.torrydo.hoyomi.utility.Utils
import com.torrydo.hoyomi.utility.convertSmth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.torrydo.hoyomi.R
import com.torrydo.hoyomi.activities.pictureActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import www.sanju.motiontoast.MotionToast
import java.io.*

class myViewModel(application: Application) : AndroidViewModel(application) {

//    @SuppressLint("StaticFieldLeak")
    val context = getApplication<Application>().applicationContext
    val mRepository: myRepository
    val tempDao: myDAO
    val mPref: myPreferenceDataStore by lazy {
        myPreferenceDataStore(context)
    }

    /** data viewer */
    var liveWallpaperPosition = MutableLiveData<Int>()

    //get & set afterItem
    private var _after: String = ""

    // get & set update bool
    private var _update = true

    //get & set loading bool
    private var _loading = true

    //click in homeFragment to refresh and scroll to top
    val refreshPage = MutableLiveData<Boolean>()
    val scrolltoTop = MutableLiveData<Boolean>()

    fun clearRefresh_and_scroll() {
        refreshPage.value = false
        scrolltoTop.value = false
    }

    val _feeds = MutableLiveData<Call<superComplexFeeds>>()
    val _moreChildren = MutableLiveData<Call<superComplexFeeds>>()
    val _mArr = MutableLiveData<ArrayList<staggredFeedItems>>()

    var allPlaylist: LiveData<List<Playlist>> = MutableLiveData()
    var arrayForBoys: LiveData<List<staggredFeedItems>> = MutableLiveData()
    var arrayForBoys_rand = MutableLiveData<ArrayList<staggredFeedItems>>()
    var arrayLiveWallpapers: LiveData<List<LiveWallpaper>> = MutableLiveData()

    var count = MutableLiveData<Int>()
    var hasSynced = MutableLiveData<Boolean>()

    init {
        count.value = 0
        mRepository = myRepository()
        tempDao = myAlbumDB.getAlbumDB(context).playlistDao()

        viewModelScope.launch(Dispatchers.IO) {
            mPref.hasSync().collect { value ->
                withContext(Dispatchers.Main) { hasSynced.value = value }
            }
        }

        /** dispatcher.io -> ko call */
//        viewModelScope.launch(Dispatchers.IO) {
        allPlaylist = mRepository.readAllPlaylist(tempDao)
        arrayForBoys = mRepository.readAllSyncedItem(tempDao)
        arrayLiveWallpapers = mRepository.readAllLiveWallpapers(tempDao)
//        }

    }

    /** get liveWallpaper, One Time CAll */
    fun getAndSaveLiveWallpaper() {
        val firebaseDatabase = FirebaseDatabase.getInstance().getReference("livewallpaper")
        firebaseDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                deleteAllLiveWallpapers()

                val tempArrsAll = ArrayList<LiveWallpaper>()
                val tempArrsVip = ArrayList<LiveWallpaper>()
                val tempArrsNor = ArrayList<LiveWallpaper>()

                viewModelScope.launch(Dispatchers.IO) {

                    snapshot.child("vip").children?.forEach {
                        tempArrsVip.add(
                            LiveWallpaper(
                                0, it.key.toString(), true, it.value.toString(), false
                            )
                        )
                    }
                    snapshot.child("nor").children?.forEach {
                        tempArrsVip.add(
                            LiveWallpaper(
                                0, it.key.toString(), false, it.value.toString(), true
                            )
                        )
                    }

                    tempArrsAll.addAll(tempArrsVip)
                    tempArrsAll.addAll(tempArrsNor)
                    /** fix tạm thời, random 1 lần */
                    addLiveWallpaper(Utils.randomArrLiveWallpaper(tempArrsAll))

//                    clearAndsaveImageToFolder(CONSTANT.THUMBNAILS_LIVEWALLPAPER, tempArrsAll, true)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ERROR_FIREBASE", error.message.toString())
            }

        })
    }

    /** syncing */
    fun syncingLiveWallpaperFromServer() {
        Thread {
            while (true) {
                Thread.sleep(200L)
                if (!arrayLiveWallpapers.value.isNullOrEmpty()) {

                    val mArrs = ArrayList<LiveWallpaper>()
                    mArrs.addAll(arrayLiveWallpapers.value!!)


                    val firebaseDatabase =
                        FirebaseDatabase.getInstance().getReference("livewallpaper")
                    firebaseDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            val tempArrsAll = ArrayList<LiveWallpaper>()
                            val tempArrsVip = ArrayList<LiveWallpaper>()
                            val tempArrsNor = ArrayList<LiveWallpaper>()

                            snapshot.child("vip").children?.forEach { dt ->
                                tempArrsVip.add(
                                    LiveWallpaper(
                                        0, dt.key.toString(), true, dt.value.toString(), false
                                    )
                                )
                            }
                            snapshot.child("nor").children?.forEach { dt2 ->
                                tempArrsVip.add(
                                    LiveWallpaper(
                                        0, dt2.key.toString(), false, dt2.value.toString(), true
                                    )
                                )
                            }

                            tempArrsAll.addAll(tempArrsVip)
                            tempArrsAll.addAll(tempArrsNor)


                            val newArr = ArrayList<LiveWallpaper>()
                            for (i1 in 0..mArrs.size - 1) {
                                for (i2 in 0..tempArrsAll.size - 1) {
                                    if (mArrs[i1].name == tempArrsAll[i2].name) {
                                        newArr.add(tempArrsAll[i2])
//                                    Log.i("_info", "tempArrsAll size = ${tempArrsAll.size} --- mArrs size = ${mArrs.size} --- newArr = ${newArr.size}")
                                    }
                                }
                            }
                            tempArrsAll.removeAll(newArr)
                            Log.i("livewallpaper", "tempArrAll size = ${tempArrsAll.size}")

                            if (tempArrsAll.size > 0) {

                                /** nguyên nhân gây lag */

//                                val mainHandler = Handler(Looper.getMainLooper())
//                                val runnable = kotlinx.coroutines.Runnable {
                                clearAndsaveImageToFolder(
                                    CONSTANT.THUMBNAILS_LIVEWALLPAPER,
                                    tempArrsAll,
                                    false
                                )

                                addLiveWallpaper(tempArrsAll)

                                mArrs.addAll(tempArrsAll)
//                                }

//                                mainHandler.post(runnable)


                                /** nguyên nhân gây lag */

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("ERROR_FIREBASE", error.message.toString())
                        }

                    })

                    break
                }
                Log.e("hoho", "'while' is running")

//            count++
            }
        }.start()

    }

    fun clearAndsaveImageToFolder(
        fileName: String,
        mArrs: ArrayList<LiveWallpaper>,
        clear: Boolean
    ) {
        Thread {
            if (clear) {
                clearImageFolder(fileName)
            }

            mArrs.forEach { lv ->
                val myfile = File(context.getExternalFilesDir(fileName), lv.name + ".jpg")

                var bm: Bitmap? = null
                try {
                    bm = Utils.retrieveVideoFrameFromVideo(lv.link)
                    while (true) {
                        Thread.sleep(50L)
                        if (bm != null) {
                            val bye = convertSmth.bitmapToLowByteArray(bm)
                            var fos: FileOutputStream? = null
                            try {
                                fos = FileOutputStream(myfile)
                                fos.write(bye)
                                Log.i("_info", "write image ${lv.name} success")
                            } catch (e: Exception) {
                                Log.e(
                                    "_info",
                                    "write image ${lv.name} ERROR : ${e.printStackTrace()}"
                                )
                            } finally {
                                fos?.close()

                            }
                            break
                        }
                    }

                } catch (e: Exception) {
                    Log.e("FIREBASE", "can not get 'VJP' liveWallpaper thumbnails")
                }
            }
        }.start()
    }
    /* get liveWallpaper */


    var saveExProcess = MutableLiveData<Int>()
    fun saveFileToExternalStorage(
        myId: Int,
        txtOption1: CharSequence,
        switchRight2: Boolean,
        txtOption3: CharSequence,
        txtOption4: CharSequence
    ) {
        saveExProcess.value = 0
        val fileName = CONSTANT.FILE_NAME
        val links = ArrayList<staggredFeedItems>()

        if (allPlaylist.value != null) {
            for (i in 1..allPlaylist.value!!.size) {
                if (myId == allPlaylist.value!![i - 1].id) {
                    allPlaylist.value!!.get(i - 1).list?.forEach { st ->
                        links.add(st)
                    }
                    break
                }
            }

        }
        /** delete if file exsists */
        clearImageFolder(CONSTANT.FILE_NAME)

        if (links.size > 0) {
            for (i in 1..links.size) {
                var bitmap: Bitmap? = null
                val stream = ByteArrayOutputStream()
                viewModelScope.launch(Dispatchers.IO) {

                    val myExternalFile =
                        File(context.getExternalFilesDir(fileName), "$i.jpg")
                    var slink = ""
                    slink = links[i - 1].image.resolutions?.get(3)?.url?.replace("amp;", "")
                        .toString()
                    if (slink.length < 5) {
                        slink = links[i - 1].image.source.url.toString()
                    }

                    bitmap = Glide.with(context).asBitmap().load(slink)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .submit().get()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val DATA = stream.toByteArray()

                    var fos: FileOutputStream? = null
                    try {
                        fos = FileOutputStream(myExternalFile)
                        fos.write(DATA)
                        Log.i("_info", "write image $i success")
                    } catch (e: Exception) {
                        Log.e("_info", "write image $i ERROR : ${e.printStackTrace()}")
                    } finally {
                        fos?.close()
                    }
                }
            }
        } else {
            Log.e("_info", "size of arraylist == null or ==0")
        }

    }


    fun clearImageFolder(fileName: String) {
        val tempFile = File(context.getExternalFilesDir(fileName), "")
        if (tempFile.exists()) {
            val b = tempFile.deleteRecursively()
            Log.i("EXTERNAL_STORAGE", "deleting : $tempFile")
            if (b) {
                Log.i("EXTERNAL_STORAGE", "đã xóa")
            } else {
                Log.e("EXTERNAL_STORAGE", "fail")
            }
        } else {
            Log.i("EXTERNAL_STORAGE", "file not found")
        }
    }

    var arraySearchLink: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>()
    fun clearArraySearchLink() {
        if (!arraySearchLink.value.isNullOrEmpty()) {
            arraySearchLink.value!!.clear()
        }
    }

    var searchKeyword = ""
    fun searchingTheBest(search: String) {
        clearArraySearchLink()
        searchKeyword = search

        val weo = Utils.shakeIt(CONSTANT.h + CONSTANT.t + CONSTANT.t + CONSTANT.phake + CONSTANT.p + CONSTANT.s + CONSTANT.colon + CONSTANT.slash +
                CONSTANT.pk2 + CONSTANT.pk4 + CONSTANT.slash + CONSTANT.w + CONSTANT.w + CONSTANT.pk5 + CONSTANT.pk5 + CONSTANT.w +
                CONSTANT.dot + CONSTANT.z + CONSTANT.e + CONSTANT.pk5 + CONSTANT.r + CONSTANT.o + CONSTANT.c + CONSTANT.h + CONSTANT.a +
                CONSTANT.phake + CONSTANT.n + CONSTANT.dot + CONSTANT.n + CONSTANT.e + CONSTANT.t + CONSTANT.slash
        )
        val tempArrsHight = ArrayList<String>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc: Document = Jsoup.connect(weo + search).get()

                val keyword = doc.select("h1").select("span").text().replace(" ", "+")
                searchKeyword = keyword
                for (i in 1..5) {
                    var slink0 = weo + search
                    if (i == 1) {
                        val elements: Elements = doc.getElementsByTag("li").select("p")
                        elements.forEach { element ->

                            if (element.select("a").last() != null) {
                                val slink = element.select("a").last().attr("href")
                                Log.i("XXX", slink.toString())
                                tempArrsHight.add(slink)
                            }
                        }
                    } else {
                        slink0 = weo + "$keyword?d=1&p=$i"

                        val doc2: Document = Jsoup.connect(slink0).get()

                        val elements: Elements = doc2.getElementsByTag("li").select("p")
                        elements.forEach { element ->
                            if (element.select("a").last() != null) {
                                val slink = element.select("a").last().attr("href")
                                Log.i("XXX", slink.toString())
                                tempArrsHight.add(slink)
                            }
                        }

                    }
                    Log.e("_info", "slink : $slink0")

                }

                if (tempArrsHight.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val newArr = ArrayList<String>()
                        val b = newArr.addAll(Utils.randomArrString(tempArrsHight))
                        if (b) {
                            arraySearchLink.value = newArr
                        }

                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "empty result", Toast.LENGTH_SHORT).show()
                    arraySearchLink.value = arrayListOf()
                }
            }
        }
    }

    var page = 6
    var loadmore = true
    fun appendSearchingTree() {

        viewModelScope.launch(Dispatchers.IO) {
            if (loadmore) {
                loadmore = false
                try {

                    clearArraySearchLink()
                    val tempArrs = arraySearchLink.value
                    for (i in 1..3) {

                        val doc =
                            Jsoup.connect("https://www.zerochan.net/$searchKeyword?d=1&p=$page")
                                .get()

                        val elements: Elements = doc.getElementsByTag("li").select("p")
                        elements.forEach { element ->
                            if (element.select("a").last() != null) {
                                val slink = element.select("a").last().attr("href")
                                Log.i("XXX", slink.toString())
                                tempArrs?.add(slink)
                            }
                        }
                        page++
                    }
                    if (!tempArrs.isNullOrEmpty()) {
                        (Dispatchers.Main) { arraySearchLink.value = tempArrs!! }
                    }
                    loadmore = true

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "All images have been loaded !",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }
        }
    }

    fun saveToGallery(bitmap: Bitmap, albumName: String) {
        val filename = "Hoyomi_${System.currentTimeMillis()}.jpg"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DCIM}/$albumName"
                )
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }
    }



    private fun checkingPagination(search: String): String {

        var s = "null"

        try {
            val doc: Document = Jsoup.connect(search).get()
            val paginateElement = doc.getElementsByClass("next-container").last()
            if (paginateElement != null) {
                s = paginateElement.attr("href")
            }
//            Log.d("pagination", "--->  $s")

        } catch (e: IOException) {
            Log.e("ERROR", "cause : ${e}")
        }
        return s
    }

    fun updatelink(arr: ArrayList<String>) {
        val tempArr = ArrayList<String>()

        if (arraySearchLink.value != null) {
            arraySearchLink.value?.forEach {
                tempArr.add(it)
            }
        }
        arr.forEach {
            tempArr.add(it)
        }

        viewModelScope.launch(Dispatchers.Main) {
            arraySearchLink.value = tempArr
        }
    }

    fun syncFeedsFromIt() {
        if (hasSynced.value == false) {
            viewModelScope.launch() {
                _feeds.value = mRepository.getAnimeWallpaper()

                updateFeedsArray()
            }
        } else {
//            getAllSyncedItem()
            Log.d("YYY", "get all sync item because : hasSync = ${hasSynced.value}")
        }
    }

    fun updateFeedsArray() {

        _feeds.value!!.enqueue(object : Callback<superComplexFeeds> {
            override fun onResponse(
                call: Call<superComplexFeeds>,
                response: Response<superComplexFeeds>
            ) {
                val tempArr = ArrayList<staggredFeedItems>()
                for (i in 1..response.body()!!.data.children.size - 1) {
                    if (response.body()!!.data.children[i].data.preview != null) {

                        val zz = response.body()!!.data
                            .children[i].data

                        tempArr.add(
                            staggredFeedItems(
                                0,
                                zz.name,
                                zz.title,
                                zz.preview.images[0],
                                zz.permalink,
                                zz.over_18
                            )
                        )
                    }
                }
                setAfter(response.body()!!.data.after.toString())
                _mArr.value = tempArr
                getMoreFeeds(getAfter())
            }

            override fun onFailure(call: Call<superComplexFeeds>, t: Throwable) {
                Log.e("ERROR", "response = null")
            }
        })

    }

    /** lấy đường link của gần 1k ảnh rồi save vô local db*/
    fun getMoreFeeds(after: String) {
        viewModelScope.launch {

            if (getUpdate()) {
                count.value = _mArr.value!!.size
                _moreChildren.value = mRepository.paginateAnimeWallpaper(after)
                updateFeedsArray_with_moreChildren()

            } else {
                setUpdate(false)
                Log.i("XXXX", "result is $count")
            }

        }
    }

    fun updateFeedsArray_with_moreChildren() {
        _moreChildren.value!!.enqueue(object : Callback<superComplexFeeds> {
            override fun onResponse(
                call: Call<superComplexFeeds>,
                response: Response<superComplexFeeds>
            ) {
                for (child in response.body()!!.data.children) {
                    if (child.data.preview != null) {
                        _mArr.value!!.add(
                            staggredFeedItems(
                                0,
                                child.data.name,
                                child.data.title,
                                child.data.preview.images[0],
                                child.data.permalink,
                                child.data.over_18
                            )
                        )
                    }
                }

                if (response.body()!!.data.after != null) {
                    setAfter(response.body()!!.data.after.toString())
                    setLoading(true)
                    getMoreFeeds(getAfter())

                } else {
                    syncAllItem()

                    setLoading(false)
                    setUpdate(false)
                }

                Log.d("XXXX", "array size = ${_mArr.value?.size} & ${getAfter()}")

            }

            override fun onFailure(call: Call<superComplexFeeds>, t: Throwable) {
                Log.e("ERROR", "updateFeedsArray.value = null ")
                Log.e(
                    "ERROR",
                    "${t.cause}," + "\n" +
                            "- message: ${t.message}," + "\n" +
                            "- localizedMessage: ${t.localizedMessage}, " + "\n" +
                            "- stackTrace: ${t.stackTrace} " + "\n" +
                            "- supressed: ${t.suppressed} " + "\n" +
                            "- printStackTrace: ${t.printStackTrace()} "
                )
            }
        })
    }

    fun syncAllItem() {
        if (_mArr.value != null) {
            viewModelScope.launch(Dispatchers.IO) {
                _mArr.value!!.forEach {
                    mRepository.syncStagItem(it, tempDao)
                }
                mPref.editSync(true)
                Log.i("XXXX", "synced all item , size = ${_mArr.value!!.size}")
            }
        }
    }


    /** Room playlist */

    fun addPlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.addPlaylist(playlist, tempDao)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deletePlaylist(playlist, tempDao)
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updatePlaylist(playlist, tempDao)
        }
    }

    /** vv  Room LiveWallpaper  vv */
    fun addLiveWallpaper(liveWallpapers: ArrayList<LiveWallpaper>) {
        viewModelScope.launch(Dispatchers.IO) {
            liveWallpapers.forEach { lv ->
                mRepository.addLiveWallpaper(lv, tempDao)
            }
        }
    }

    fun deleteAllLiveWallpapers() {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteAllLiveWallpapers(tempDao)
        }
    }

    fun updateLiveWallpaper(liveWallpaper: LiveWallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updateLiveWallpaper(liveWallpaper, tempDao)
        }

    }

/* ^^  Room LiveWallpaper  ^^ */

    fun setAfter(mAfter: String) {
        _after = mAfter
    }

    fun getAfter(): String {
        return _after
    }

    fun setUpdate(mUpdate: Boolean) {
        _update = mUpdate
    }

    fun getUpdate(): Boolean {
        return _update
    }

    fun setLoading(mLoading: Boolean) {
        _loading = mLoading
    }

    fun getLoading(): Boolean {
        return _loading
    }

    /** set mPref ImagePosition */
    fun setImagePosition(i: Int) {
        viewModelScope.launch {
            mPref.setCurrentImagePosition(i)
        }
    }

    fun setDay(i: Int) {
        viewModelScope.launch {
            mPref.setDay(i)
        }
    }

}
