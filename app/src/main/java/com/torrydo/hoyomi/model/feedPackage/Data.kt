package com.torrydo.hoyomi.model.feedPackage

data class Data(
    val after: String,
    val before: Any,
    val children: List<Children>,
    val dist: Int,
    val modhash: String
)