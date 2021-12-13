package com.torrydo.hoyomi.localData.roomDB

import androidx.room.TypeConverter
import com.torrydo.hoyomi.model.feedPackage.Image
import com.torrydo.hoyomi.model.staggredFeedItems
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

        @TypeConverter
        fun fromList_to_String(list: List<staggredFeedItems>): String {
                val type = object: TypeToken<List<staggredFeedItems>>() {}.type
                return Gson().toJson(list, type)
        }

        @TypeConverter
        fun fromString_to_List(value: String): List<staggredFeedItems> {
                val type = object: TypeToken<List<staggredFeedItems>>() {}.type
                return Gson().fromJson(value, type)
        }

        @TypeConverter
        fun fromImage_to_String(img: Image): String {
                val type = object: TypeToken<Image>() {}.type
                return Gson().toJson(img, type)
        }

        @TypeConverter
        fun fromString_to_Image(value: String): Image {
                val type = object: TypeToken<Image>() {}.type
                return Gson().fromJson(value, type)
        }



//        @TypeConverter
//        fun listToJson(value: List<staggredFeedItems>) : String = Gson().toJson(value)
//
//        @TypeConverter
//        fun jsonToList(value: String) : List<staggredFeedItems> = Gson().fromJson(value, Array<staggredFeedItems>::class.java).toMutableList()


}
