package com.torrydo.hoyomi.utility

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.developer.spoti.vspoti.VSpotView
import java.io.ByteArrayOutputStream

class convertSmth {

    companion object{

        fun replaceAmp(str:String) : String{
            return str.replace("amp;","")
        }

        fun bitmapToLowByteArray(bitmap :Bitmap):ByteArray{

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            val image = stream.toByteArray()

            return image
        }
        fun byteArrayToBitmap(bye : ByteArray) : Bitmap{
            return BitmapFactory.decodeByteArray(bye,0,bye.size)
        }

        fun HightReso_to_LowReso(slink:String):String{
            return slink.replace("static","s1").replace("full","600").replace("png","jpg")
        }
        fun ImageToHightByteArray(image: ImageView): ByteArray {
            val bitmap = (image.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

            return stream.toByteArray()
        }

    }

}