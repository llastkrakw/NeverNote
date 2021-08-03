package com.llastkrakw.nevernote.core.extension

import android.provider.MediaStore
import com.llastkrakw.nevernote.core.constants.*


fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)
fun String.getParentPath() = removeSuffix("/${getFilenameFromPath()}")
fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.isImageFast() = photoExtensions.any { endsWith(it, true) }
fun String.isAudioFast() = audioExtensions.any { endsWith(it, true) }
fun String.isRawFast() = rawExtensions.any { endsWith(it, true) }
fun String.isVideoFast() = videoExtensions.any { endsWith(it, true) }

fun String.isImageSlow() = isImageFast() || getMimeType().startsWith("image") || startsWith(
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
fun String.isVideoSlow() = isVideoFast() || getMimeType().startsWith("video") || startsWith(
    MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString())
fun String.isAudioSlow() = isAudioFast() || getMimeType().startsWith("audio") || startsWith(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())
