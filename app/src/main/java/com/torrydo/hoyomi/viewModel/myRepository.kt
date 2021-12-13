package com.torrydo.hoyomi.viewModel

import androidx.lifecycle.LiveData
import com.torrydo.hoyomi.INSTANCE
import com.torrydo.hoyomi.localData.roomDB.myDAO
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.feedPackage.superComplexFeeds
import com.torrydo.hoyomi.model.staggredFeedItems
import retrofit2.Call

class myRepository() {

    suspend fun getAnimeWallpaper() : Call<superComplexFeeds>{
        return INSTANCE.myApi.getAnimeWallpaper(100)
    }
    suspend fun paginateAnimeWallpaper(after : String) : Call<superComplexFeeds>{
        return INSTANCE.myApi.paginateAnimeWallpaperChildren(after,100)
    }


    /** Playlist */
    suspend fun addPlaylist(playlist: Playlist, myDAO: myDAO){
        myDAO.addPlaylist(playlist)
    }
    suspend fun updatePlaylist(playlist: Playlist, myDAO: myDAO){
        myDAO.updatePlaylist(playlist)
    }
    suspend fun deletePlaylist(playlist: Playlist, myDAO: myDAO){
        myDAO.deletePlaylist(playlist)
    }

    fun readAllPlaylist(plDao: myDAO) : LiveData<List<Playlist>> {
        return plDao.readAllPlaylist()
    }

    fun readOnePlaylist(plDao: myDAO) : LiveData<List<Playlist>> {
        return plDao.readAllPlaylist()
    }


    /** item synced */
    suspend fun syncStagItem(stag: staggredFeedItems, myDAO: myDAO){
        myDAO.syncItems(stag)
    }
    fun readAllSyncedItem(plDao: myDAO) : LiveData<List<staggredFeedItems>> {
        return plDao.readAllSyncedItem()
    }


    /** LiveWallpaper */
    suspend fun addLiveWallpaper(liveWallpaper: LiveWallpaper, myDAO: myDAO){
        myDAO.addLiveWallpaper(liveWallpaper)
    }
    fun readAllLiveWallpapers(plDao: myDAO) : LiveData<List<LiveWallpaper>> {
        return plDao.readAllLiveWallpapers()
    }
    suspend fun deleteAllLiveWallpapers(myDAO: myDAO) = myDAO.deleteAllLiveWallpapers()

    suspend fun updateLiveWallpaper(liveWallpaper: LiveWallpaper, myDAO: myDAO){
        myDAO.updateLiveWallpaper(liveWallpaper)
    }

}