package com.sawears.diagnostics

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch


/*
 * Tracks processing latency for real time audio by recording
 * timestamps for audio packets at different stages
 */
interface RealTimeLatencyTracker{

    fun recordAtStage(
        id: Long,
        stage: RealTimeLatencyStage,
        timeStampNanos: Long
    )
    fun recordAtStage(
        id: Long,
        stage: RealTimeLatencyStage
    )
}



/*
 * No op version to use in production
 */
class NoOpRealTimeLatencyTracker: RealTimeLatencyTracker {
    override fun recordAtStage(
        id: Long,
        stage: RealTimeLatencyStage,
        timeStampNanos: Long
    ) = Unit
    override fun recordAtStage(
        id: Long,
        stage: RealTimeLatencyStage
    ) = Unit
}

interface RealTimeLatencyResultsWriter{
    fun write(traces:  MutableMap<Long, MutableMap<RealTimeLatencyStage, Long>>)
    fun close()
    fun flush()
}

class NoOpLatencyResultsWriter: RealTimeLatencyResultsWriter{
    override fun write(traces:  MutableMap<Long, MutableMap<RealTimeLatencyStage, Long>>) = Unit
    override fun close() = Unit
    override fun flush() = Unit
}

/*
 * Live version for diagnostics
 *
 * Send marks to a writer class and terminates when a fixed amount of
 * data has been written
 *
 * @param scope [CoroutineScope] in which to execute
 * @param writer a [ResultsWriter] to handle writing results to some output
 * @param limit stop after receiving this many samples
 */
class DiagnosticRealTimeLatencyTracker(
    writer: ResultsWriter,
    limit: Int=3_000,
    lastStage: RealTimeLatencyStage = RealTimeLatencyStage.CURVE_DRAWING_COMPLETED
): RealTimeLatencyTracker{
    private val marks = Channel<RealTimeLatencyMark>(
        capacity = 4096,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val scope = CoroutineScope(
        SupervisorJob() +
                Dispatchers.IO +
                CoroutineName("LatencyTracking")
    )

    init {
        scope.launch(Dispatchers.Default) {

            var totalMarksProcessed = 0
            var marksSinceFlush = 0

            for (mark in marks) {
                writer.write(mark)
                marksSinceFlush++
                // only increment total if sample at final tracking stage
                if (mark.stage == lastStage) totalMarksProcessed++
                if (marksSinceFlush > 500){ // flush writer every 500 marks
                    writer.flush()
                    marksSinceFlush = 0
                }

                // stop once sufficient samples collected
                if (totalMarksProcessed > limit){
                    writer.flush()
                    writer.close()
                    println("data captured")
                    break
                }
            }
        }
    }

    override fun recordAtStage(
        id: Long,
        stage: RealTimeLatencyStage,
        timeStampNanos: Long
    ) {
        marks.trySend(
            RealTimeLatencyMark(
                id,
                timeStampNanos,
                stage
            )
        )
    }
    override fun recordAtStage(
        id: Long,
        stage: RealTimeLatencyStage
    ) {
        recordAtStage(
            id,
            stage,
            System.nanoTime()
        )
    }
}


/*
 * Represents the timestamp for a single audio frame at a single stage.
 *
 * Based on approach to latency tracking found here, but with added ID to track
 * a single frame over time:
 * https://docs.rs/vona/latest/vona/struct.RealtimeLatencyMark.html
 *
 * @param id the id of the audio frame
 * @param timeStampNanos
 * @param stage the stage at which it was captured
 */
data class RealTimeLatencyMark(
    val id: Long,
    val timeStampNanos: Long,
    val stage: RealTimeLatencyStage
)

interface ResultsWriter{
    fun write(mark: RealTimeLatencyMark)
    fun flush()
    fun close()
}


/*
 * Represents the stage at which a latency measurement was taken
 *
 * Based on similar approaches to tracking latency, e.g.:
 * https://docs.rs/vona/latest/vona/enum.RealtimeLatencyStage.html
 */
enum class RealTimeLatencyStage {
    FRAME_READ,
    ESTIMATION_STARTED,
    ESTIMATION_COMPLETE, // pitch result produced
    ANIMATION_STATE_EMITTED,
    CURVE_DRAWING_STARTED,
    CURVE_DRAWING_COMPLETED
}