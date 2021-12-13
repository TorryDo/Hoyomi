package com.torrydo.hoyomi.interfaces

import com.torrydo.hoyomi.adapter.adapter_video

interface retrieveVideoDownloadRequest {
    fun clicked(position:Int,holder: adapter_video.videoHolder)
}