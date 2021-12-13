package com.torrydo.hoyomi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.torrydo.hoyomi.CONSTANT

@Entity(tableName = CONSTANT.PLAYLIST_NAME)
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val playlistTitle:String,
    val date:String,
    val list:List<staggredFeedItems>?,
    val lastModifided : String?,
    val dailyTime:String?,
    val hasNoti:Boolean?,
    val order:String?,
    val apply:String?

)
