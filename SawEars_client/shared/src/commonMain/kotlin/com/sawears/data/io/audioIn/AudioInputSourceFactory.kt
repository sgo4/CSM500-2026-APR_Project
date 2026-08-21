package com.sawears.data.io.audioIn

/**
 * Factory class to provide application specific audioinput source.
 *
 * Necessary to avoid injecting dependencies that may require runtime permissions directly.
 * Instead, the factory is injected and the actual source can be created after permissions
 * are secured.
 */
fun interface AudioInputSourceFactory {
    fun create(): AudioInputSource
}