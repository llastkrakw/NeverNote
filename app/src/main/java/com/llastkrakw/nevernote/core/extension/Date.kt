package com.llastkrakw.nevernote.core.extension

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun Date.dateExpired() : Boolean {
    Log.d("date_issue", "expired is ${this.time < System.currentTimeMillis()}")
    return this.time < System.currentTimeMillis()
}
