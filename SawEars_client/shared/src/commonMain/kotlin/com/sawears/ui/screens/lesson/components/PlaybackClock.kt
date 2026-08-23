package com.sawears.ui.screens.lesson.components

class PlaybackClock {

    private var anchorFrameNanos = 0L
    private var anchorSystemNanos: Long? = null

    /**
     * When the clock is started anchor is set to current system time
     */
    fun start(currentTimeNanos: Long = getCurrentSystemNanos()){
        anchorSystemNanos = currentTimeNanos
    }

    fun stop(currentTimeNanos: Long = getCurrentSystemNanos()){
        anchorFrameNanos = positionAtTime(currentTimeNanos)
        anchorSystemNanos = null
    }

    /**
     * Wrapper for system time call, allowing a different fundamental method to be subbeed in more easily
     */
    fun getCurrentSystemNanos(): Long {
        return System.nanoTime()
    }

    fun positionAtTime(currentTimeNanos: Long = System.nanoTime()): Long {

        val playbackStarted = anchorSystemNanos ?: return anchorFrameNanos

        return anchorFrameNanos + (currentTimeNanos - playbackStarted)
    }
}