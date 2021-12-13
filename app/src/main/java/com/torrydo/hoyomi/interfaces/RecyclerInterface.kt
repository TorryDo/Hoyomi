package com.torrydo.hoyomi.interfaces

import com.torrydo.hoyomi.databinding.ItemPlaylistBinding

interface RecyclerInterface {
    fun onItemCLick(position:Int, binding: ItemPlaylistBinding)
    fun onItemLongCLick(position:Int, binding: ItemPlaylistBinding)
}