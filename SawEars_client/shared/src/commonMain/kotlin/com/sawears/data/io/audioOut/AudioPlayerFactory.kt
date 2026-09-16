package com.sawears.data.io.audioOut

/**
 * Produces an instance of an [AudioPlayer] which may be platform specific
 *
 * Implementation note: This is used to enable each platform to generate as many
 * instances as it needs of platform specific audio I/O classes rather than being
 * passed an app level singleton.
 */
interface AudioPlayerFactory {

    fun create(): AudioPlayer
}