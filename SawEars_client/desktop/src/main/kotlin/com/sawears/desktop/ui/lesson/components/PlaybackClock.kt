package com.sawears.desktop.ui.lesson.components

class PlaybackClock {

    private var anchorFrameNanos = 0L
    private var anchorSystemNanos: Long? = null

    fun start(currentTimeNanos: Long = System.nanoTime()){
        anchorSystemNanos = currentTimeNanos
    }

    fun stop(currentTimeNanos: Long = System.nanoTime()){
        anchorFrameNanos = positionAtTime(currentTimeNanos)
        anchorSystemNanos = null
    }

    fun positionAtTime(currentTimeNanos: Long = System.nanoTime()): Long {

        val playbackStarted = anchorSystemNanos ?: return anchorFrameNanos

        return anchorFrameNanos + (currentTimeNanos - playbackStarted)
    }
}