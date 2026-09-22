package com.sawears.diagnostics





/**
 * Represents the stage at which a latency measurement was taken
 */
enum class LatencyTrackerStage {
    FIRST_SAMPLE_RECEIVED,
    FRAME_COMPLETE,
    ESTIMATION_STARTED,
    ESTIMATION_COMPLETE,
    QUEUED_UI,
    UI_UPDATED
}