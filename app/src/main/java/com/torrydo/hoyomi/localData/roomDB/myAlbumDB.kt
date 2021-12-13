package com.torrydo.hoyomi.localData.roomDB

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.model.LiveWallpaper
import com.torrydo.hoyomi.model.Playlist
import com.torrydo.hoyomi.model.staggredFeedItems

@Database(
    entities = [
        Playlist::class,
        staggredFeedItems::class,
        LiveWallpaper::class
               ],
    version = CONSTANT.ROOM_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class myAlbumDB : RoomDatabase() {

    abstract fun playlistDao() : myDAO

    companion object{
        @Volatile
        private var mAlbumDB : myAlbumDB? = null

        fun getAlbumDB(context: Context) : myAlbumDB{
            val tempInstance = mAlbumDB
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room
                    .databaseBuilder(context.applicationContext,myAlbumDB::class.java,CONSTANT.PLAYLIST_NAME)
                    .build()
                mAlbumDB = instance
                Log.d("Response","db created")
                return instance

            }
        }

    }



}