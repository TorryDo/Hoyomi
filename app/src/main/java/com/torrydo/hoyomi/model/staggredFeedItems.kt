package com.torrydo.hoyomi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.torrydo.hoyomi.CONSTANT
import com.torrydo.hoyomi.model.feedPackage.Image

@Entity(tableName = CONSTANT.Staggered_NAME)
data class staggredFeedItems (
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val name:String,
    val title:String,
    val image:Image,
    val feedsSource:String,
    val over_18:Boolean

)