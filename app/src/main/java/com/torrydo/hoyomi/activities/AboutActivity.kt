package com.torrydo.hoyomi.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.torrydo.hoyomi.R
import java.lang.ref.WeakReference

class AboutActivity : AppCompatActivity() {

    private val mContext = WeakReference<Context>(this).get()!!
    private var textView : TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(mContext,R.color.white)


        textView = findViewById(R.id.about_txt)

        val about = "Hi there, I'm glad you downloaded this app. After a long and difficult time, " +
                "the problem went away and the app was finally uploaded to Google PlayStore. " +
                "I really hope my efforts will not be in vain. Thank you for your time :) \n \n \n"+
                "- Credits: \n"+
                "Image Icon Tadah Animation by Lottiefiles on Lottiefiles \n"+
                "menhera Animation by Сергій Корсак on Lottiefiles \n"+
                "ifinite load Animation by Ilya Pavlov on Lottiefiles \n"+
                "notifications Animation by Павел Потапов on Lottiefiles \n"+
                "update para coverme Animation by Toly Tomás on Lottiefiles \n"+
                "crown coin Icon made by Freepik from www.flaticon.com \n \n \n" +
                "- Without your productions my application wouldn't have been complete, thank you from bottom of my heart <3"

        textView!!.text = about
    }

    override fun onPause() {
        super.onPause()
        if(isFinishing){
            textView = null
        }
    }
}