package com.torrydo.hoyomi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.torrydo.hoyomi.CONSTANT

@Entity(tableName = CONSTANT.THUMBNAILS)
data class LiveWallpaper(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val name:String,
    val vip:Boolean,
    val link:String,
    val unlocked:Boolean

)
