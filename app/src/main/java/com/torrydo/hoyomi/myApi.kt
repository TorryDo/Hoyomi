package com.torrydo.hoyomi

import com.torrydo.hoyomi.model.feedPackage.superComplexFeeds
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface myApi {

    @GET("Animewallpaper/.api")
    fun getAnimeWallpaper(
        @Query("limit") limit:Int
    ): Call<superComplexFeeds>

    @GET("Animewallpaper/.api")
    fun paginateAnimeWallpaperChildren(
        @Query("after") lastItem: String,
        @Query("limit") limit:Int
    ): Call<superComplexFeeds>


}