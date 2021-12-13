package com.torrydo.hoyomi

import com.torrydo.hoyomi.CONSTANT.Companion.c
import com.torrydo.hoyomi.CONSTANT.Companion.colon
import com.torrydo.hoyomi.CONSTANT.Companion.d
import com.torrydo.hoyomi.CONSTANT.Companion.dot
import com.torrydo.hoyomi.CONSTANT.Companion.e
import com.torrydo.hoyomi.CONSTANT.Companion.i
import com.torrydo.hoyomi.CONSTANT.Companion.m
import com.torrydo.hoyomi.CONSTANT.Companion.o
import com.torrydo.hoyomi.CONSTANT.Companion.r
import com.torrydo.hoyomi.CONSTANT.Companion.t
import com.torrydo.hoyomi.CONSTANT.Companion.h
import com.torrydo.hoyomi.CONSTANT.Companion.p
import com.torrydo.hoyomi.CONSTANT.Companion.phake
import com.torrydo.hoyomi.CONSTANT.Companion.pk2
import com.torrydo.hoyomi.CONSTANT.Companion.pk3
import com.torrydo.hoyomi.CONSTANT.Companion.pk4
import com.torrydo.hoyomi.CONSTANT.Companion.pk5
import com.torrydo.hoyomi.CONSTANT.Companion.s
import com.torrydo.hoyomi.CONSTANT.Companion.slash
import com.torrydo.hoyomi.CONSTANT.Companion.w
import com.torrydo.hoyomi.utility.Utils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object INSTANCE {

    val myRetrofit = Retrofit.Builder()
        .baseUrl(Utils.shakingBaseUrl(h  + pk2 + t  +  t + pk3 +  p  + phake +
                s  + pk5 + colon  +  slash  +
                slash + phake +  w + pk4 +  w + pk4 +  w  +  dot
                +  r  +  e + pk5 +  d  +  d  + phake + pk4 +   i  +  t  +
                dot  +  c + pk3  + phake + o  +  m + pk2 +  slash + pk5 +  r
                + pk2 + slash)
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val myApi = myRetrofit.create(com.torrydo.hoyomi.myApi::class.java)
}