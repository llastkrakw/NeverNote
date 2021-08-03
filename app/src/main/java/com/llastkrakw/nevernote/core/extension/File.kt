package com.llastkrakw.nevernote.core.extension

import com.llastkrakw.nevernote.core.constants.audioExtensions
import java.io.File

fun File.isAudioFast() = audioExtensions.any { absolutePath.endsWith(it, true) }