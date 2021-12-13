package com.torrydo.hoyomi.localData.roomDB

import androidx.lifecycle.LiveData
import androidx.room.*
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems

@Dao
interface myDAO {

    /** Playlist */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlaylist(playlist: Playlist)

    @Query("SELECT * FROM ${CONSTANT.PLAYLIST_NAME} ORDER BY id ASC")
    fun readAllPlaylist(): LiveData<List<Playlist>>

    @Query("SELECT * FROM ${CONSTANT.PLAYLIST_NAME} WHERE id = :mId")
    fun readOnePlaylist(mId: Int): Playlist

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
//    @Query("DELETE FROM ${CONSTANT.PLAYLIST_NAME}")
//    suspend fun deleteAllPlaylist()


    /** item synced */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun syncItems(stag: staggredFeedItems)

    @Query(" SELECT * FROM ${CONSTANT.Staggered_NAME} ORDER BY id ASC ")
    fun readAllSyncedItem(): LiveData<List<staggredFeedItems>>


    /** LiveWallpaper */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLiveWallpaper(liveWallpaper: LiveWallpaper)

    @Query("SELECT * FROM ${CONSTANT.THUMBNAILS} ORDER BY id ASC")
    fun readAllLiveWallpapers(): LiveData<List<LiveWallpaper>>

    @Query("SELECT * FROM ${CONSTANT.THUMBNAILS} WHERE id = :mid")
    fun readOneLiveWallpaper(mid:Int):LiveWallpaper

    @Update
    suspend fun updateLiveWallpaper(liveWallpaper: LiveWallpaper)

    @Query("DELETE FROM ${CONSTANT.THUMBNAILS}")
    suspend fun deleteAllLiveWallpapers()


}