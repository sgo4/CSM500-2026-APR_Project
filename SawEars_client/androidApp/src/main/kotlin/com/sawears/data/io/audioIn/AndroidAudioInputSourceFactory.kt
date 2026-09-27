package com.sawears.data.io.audioIn

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject

/**
 * Android implementation of factory of AudioInputSource
 *
 * Includes some checks for permissions handling
 * @property application and [Application] object
 */
class AndroidAudioInputSourceFactory @Inject constructor(
    private val application: Application
): AudioInputSourceFactory {

    override fun create(): AudioInputSource {
        check(
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
        return createWithPermission()
    }

    @SuppressLint("MissingPermission")
    private fun createWithPermission(): AudioInputSource {
        return AndroidMicAudioInputSource()
    }
}