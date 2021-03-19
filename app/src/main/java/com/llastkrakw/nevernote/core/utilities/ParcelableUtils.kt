package com.llastkrakw.nevernote.core.utilities

import android.os.Bundle
import android.os.Parcel

import android.os.Parcelable


inline fun <reified R : Parcelable> R.testParcel(): R? {
    val bytes = marshallParcelable(this)
    return unmarshallParcelable(bytes)
}

inline fun <reified R : Parcelable> marshallParcelable(parcelable: R): ByteArray {
    val bundle = Bundle().apply { putParcelable(R::class.java.name, parcelable) }
    return marshall(bundle)
}

fun marshall(bundle: Bundle): ByteArray =
        Parcel.obtain().use {
            it.writeBundle(bundle)
            it.marshall()
        }

inline fun <reified R : Parcelable> unmarshallParcelable(bytes: ByteArray): R? = unmarshall(bytes)
        .readBundle(ClassLoader.getSystemClassLoader())
        ?.run {
            classLoader = R::class.java.classLoader
            getParcelable(R::class.java.name)
        }

fun unmarshall(bytes: ByteArray): Parcel =
        Parcel.obtain().apply {
            unmarshall(bytes, 0, bytes.size)
            setDataPosition(0)
        }

private fun <T> Parcel.use(block: (Parcel) -> T): T =
        try {
            block(this)
        } finally {
            this.recycle()
        }