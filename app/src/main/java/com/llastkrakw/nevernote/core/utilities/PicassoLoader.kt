package com.llastkrakw.nevernote.core.utilities

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.llastkrakw.nevernote.BuildConfig
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request

fun picassoLoader(view : View, url : String){
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}")
                .build()
            chain.proceed(newRequest)
        }
        .build()

    val picasso = Picasso.Builder(view.context)
        .downloader(OkHttp3Downloader(client))
        .build()

    picasso.load(url)
        .into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                Log.d("Success", "Good")
                view.background = BitmapDrawable(view.resources, bitmap)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Log.d("Failed", e.toString())
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }

        })
}