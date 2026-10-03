package com.sawears.algorithm_development.pitch

import com.sawears.algorithm_development.io.AudioFrame
import kotlin.math.pow
import kotlin.math.log10
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.hypot
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.system.exitProcess

/**
 * Kotlin implementation of real-time Swipe based on
 * research by Peter Meier, Sebastian Strahl, Simon Schwär,
 * Meinard Müller and  Stefan Balke
 *
 * - Paper:
 *  https://www.audiolabs-erlangen.de/content/05_fau/professor/00_mueller/03_publications/2025_MeierSSMB_RealTimeSWIPE_CMMR_ePrint.pdf
 *
 * Kotlin implementation adapted from Python implementation by Peter Meier:
 * - https://github.com/groupmm/real_time_swipe
 *
 * Implementation notes:
 * - Vectorized numpy operations in the python source have beene rewritten as
 *     explicit loops for transparency and to avoid reliance on libraries
 * - Care has been taken to replicate functionality as close as possible to the original
 *     paper while adapting to the required interface structure of the surrounding code
 * - Samples MUST be normalized
 *
 * @property sampleRate sample rate in Hz
 * @property frameSize size of the frame in number of samples
 * @property fMin minimum frequency expected in Hz
 * @property fMax maximum frequency expected in Hz
 * @property erbStep Step size for ERB (Equivalent rectangular bandwidth) scale
 * @property hopLen hop length
 * @property pitchRes  pitch resolution in cents (12.5 cent = 1/96 per octave)
 * @property strThresh strength threshold
 * @property numChannels number of audio channels (1 for mono)
 * @param delay Delay factor [0, 1]
 */
class RTSwipeEstimator(
    override val sampleRate: Float,
    override val frameSize: Int,
    private val fMin: Double = 55.0,
    private val fMax: Double = 1760.0,
    private val erbStep: Double = 0.1,
    private val pitchRes: Double = 12.5,
    private val strThresh: Double = 0.0,
    private val numChannels: Int = 1,
    delay: Double = 0.0
): PitchEstimator {

    private val hopLen = frameSize
    // Helper class for parabolic interpolation
    private val interpolator = Interpolator()
    /*
    Here we differ from author's implementation by caching primes up to a large number
    on init rather than calculating every tine.
    This list can simply be sliced according to the desired number of harmonics as needed
     */
    private val primesWithOne = primesWithOne()
    private val pitchCandidates = generatePitchCandidates()
    // precompute other algorithm constants
    private val logWsMax = ceil(log2((8.0 / fMin) * sampleRate)).toInt()
    private val logWsMin = floor(log2((8.0 / fMax) * sampleRate)).toInt()
    private val windowSizes = IntArray(logWsMax - logWsMin+1) { i ->
        2.0.pow((logWsMax - i).toDouble()).toInt()
    }
    // precompute and cache an FFT object for each window size
    private val ffts = Array<DoubleFFT_1D>(windowSizes.size){
        i -> DoubleFFT_1D(windowSizes[i].toLong())
    }
    // dimensionality is to allow multi channel audio
    private val err = arrayOf(
        DoubleArray(pitchCandidates.size) { i ->
            abs(log2(8.0 * sampleRate / pitchCandidates[i]) - log2(windowSizes.max().toDouble()))
        }
    )
    private val octaves = Array(windowSizes.size){
            i -> doubleArrayOf(i.toDouble())
    }
    private val candidates = Array(octaves.size) { octave ->
        BooleanArray(err[0].size) { candidate ->
            err[0][candidate] > octaves[octave][0] - 1.0 &&
                    err[0][candidate] < octaves[octave][0] + 1.0
        }
    }
    // array of zeroes with same shape as candidates
    private val mu = generateMu()

    private val erbFreqs = generateErbFreqs(pitchCandidates)
    private val windows = Array(windowSizes.size){
            i -> periodicHanningWindow(windowSizes[i].toDouble())
    }
    val fftFreqs: Array<DoubleArray> =
        Array(windowSizes.size) { i ->
            rfft(
                windowSize = windowSizes[i],
                sampleRate = sampleRate.toDouble()
            )
        }

    // transposed vs author's implementation for performance reasons
    private val kernels = Array(pitchCandidates.size) { DoubleArray(erbFreqs.size) }

    private var buffer = Array(windowSizes.max()){
            i -> DoubleArray(numChannels){
            j -> 0.0
        }
    }
    private val delaySize = floor(delay * (windowSizes.maxOrNull()!! / 2)).toInt() // equation 7 in RT SWIPE paper

    // additional setup and validation
    init {
        // validate delay
        require(delay in 0.0..1.0){
            "The delay (factor) must be a float value between [0, 1]."
        }
        // populate kernels
        for (j in pitchCandidates.indices) {
            val kernel = computeNormalisedKernel(erbFreqs, pitchCandidates[j])

            for (i in kernel.indices) {
                kernels[j][i] = kernel[i]
            }
        }
    }

    /*
    Main estimation function

    Note: we differ from the authors' implementation by assuming mono audio
     via audio frame class for simplicity
     */
    override fun estimate(frame: AudioFrame): PitchResult {

        val samples = frame.samples

        require (samples.size == hopLen){
            "Audio input does not match hop_len"
        }
        // audio frame object enforces mono
        require (numChannels == 1){
            "Num channels is not 1 - this implementation only supports mono audio"
        }

        // populate buffer
        for (row in 0 until buffer.size - hopLen) {
            buffer[row + hopLen].copyInto(buffer[row])
        }
        for (sampleIdx in 0 until hopLen) {
            // convert dimensionality to match estimator behaviour
            doubleArrayOf(samples[sampleIdx].toDouble()).copyInto(buffer[buffer.size - hopLen + sampleIdx])
        }


        // generate X_erb
        val X_erb = generateXerb()

        // normalize to unit length
        // calculate norms
        val norms = Array(X_erb.size) { i ->
            Array(X_erb[i].size) { j ->
                var sumSquares = 0.0

                for (k in X_erb[i][j].indices) {
                    val value = X_erb[i][j][k]
                    sumSquares += value * value
                }
                var norm = sqrt(sumSquares)
                if (norm == 0.0){
                    norm = 1.0 // Replace zero norms with one to avoid division by zero
                }
                // mimic np keepdims=True, i.e. the last dimension has size 1
                doubleArrayOf(norm)
            }
        }
        // divide X_erb by norms
        for (i in X_erb.indices){
            for (j in X_erb[i].indices){
                val norm = norms[i][j][0]
                for (k in X_erb[i][j].indices){
                    X_erb[i][j][k] /= norm
                }
            }
        }

        // Correlate spectra with kernels
        val S_all = correlateSpectra(X_erb, kernels)

        // Merge pitch strengths computed for different window sizes
        val S = mergePitchStrengths(S_all)
        // Fine tune the pitch using parabolic interpolation
        val pitch = interpolator.parabolicInterpolation(
            transpose2d(S),
            strThresh,
            pitchCandidates
            )
        // Avoid NaN output (assumes pitches and strengths are same length)
        for (i in pitch.strengths.indices){
            if (pitch.strengths[i] == Double.NaN){
                pitch.strengths[i] = 0.0
            }
            if (pitch.pitches[i] == Double.NaN){
                pitch.pitches[i] == Double.NaN
            }
        }
        // here we differ from the authors' interpretation by returning a single candidate
        // based on max strength
        var indexOfStrongestPitch = 0
        var maxStrength = Double.NEGATIVE_INFINITY
        for (i in pitch.strengths.indices){
            val strength = pitch.strengths[i]
            if (strength > maxStrength){
                maxStrength = strength
                indexOfStrongestPitch = i
            }
        }


        return PitchResult(
            pitch.pitches[indexOfStrongestPitch],
            frame.timeStamp,
            maxStrength
        )
    }

    private fun generatePitchCandidates(): DoubleArray {
        val logFmin = log2(fMin)
        val logFmax = log2(fMax)
        val step = pitchRes / 1200
        val pitchCandidates = mutableListOf<Double>()
        var logF = logFmin
        while (logF < logFmax){
            pitchCandidates += 2.0.pow(logF)
            logF += step
        }
        return pitchCandidates.toDoubleArray()
    }

    /*
    Convert Hertz measurements to ERB scale

    @param hz
    @return ERB scale measurement
     */
    private fun hzToErbs(hz: Double): Double{
        return 21.4 * log10(1 + hz / 229)
    }

    /*
    Convert ERB scale measurements to Hertz
    @param erbs
    @return Hz measurement
     */
    private fun erbsToHz(erbs: Double): Double{
        return (10.0.pow(erbs/ 21.4) - 1) * 229
    }

    private fun generateErbFreqs(pitchCandidates: DoubleArray): DoubleArray {
        val res = mutableListOf<Double>()

        val start = hzToErbs(pitchCandidates[0] / 4.0)
        val stop = hzToErbs(sampleRate / 2.0)

        var i = start
        while (i < stop){
            res += erbsToHz(i)
            i += erbStep
        }

        return res.toDoubleArray()
    }

    /*
    Compute kernel for a single pitch candidate

    @param erbFreqs
    @param pitchCandidate
    @return normalized kernel for pitch candidate
     */
    private fun computeNormalisedKernel(erbFreqs: DoubleArray, pitchCandidate: Double): DoubleArray {

        val numHarmonics = floor(
            erbFreqs.last() / pitchCandidate - 0.75
        ).toInt()

        require(erbFreqs.isNotEmpty()) {
            "erbFreqs must not be empty"
        }
        require(pitchCandidate > 0.0) {
            "pitchCandidate must be greater than zero"
        }

        val k = DoubleArray(erbFreqs.size)
        val q = DoubleArray(erbFreqs.size) { i ->
            erbFreqs[i] / pitchCandidate
        }
        // use precomputed primes here for speed
        for (harmonic_idx in 0 until numHarmonics) {
            val harmonic = primesWithOne[harmonic_idx]
            for (i in q.indices) {
                val distance = abs(q[i] - harmonic) // difference between harmonic and q[i
                val cos = cos(2.0 * PI * q[i])
                if (distance < 0.25){
                    k[i] = cos // set kernel index to full cosine for frequencies very close to harmonic
                } else if (distance > 0.25 && distance < 0.75) {
                    k[i] += cos / 2.0 // set kernel index to half cosine for frequencies less close to harmonic
                } else {
                }
            }
        }

        // Apply envelope: k *= sqrt(1 / q)
        for (index in k.indices) {
            k[index] *= sqrt(1.0 / q[index])
        }

        // L2 normalise kernel
        var norm = l2NormOfPositiveElements(k)
        if (norm > 0.0) {
            for (index in k.indices) {
                k[index] /= norm
            }
        }

        return k
    }

    /*
    Helper function to calculate the L2 norm of kernel vector.
    In plain terms it calculates sqrt(sum of squares of positive elements).
    @pparam vector
    @return l2 norm
     */
    private fun l2NormOfPositiveElements(vector: DoubleArray): Double {
        // variable to store the cumulative square of positive elements of vector
        var squareOfPositive = 0.0
        // sum of squares of positive elements
        for (value in vector) {
            if (value > 0.0) {
                squareOfPositive += value * value
            }
        }
        return sqrt(squareOfPositive)
    }

    /*
    Helper function to generate mu
     */
    private fun generateMu(): Array<DoubleArray>{
        val mu = Array(candidates.size) { i ->
            DoubleArray(candidates[i].size)
        }

        val cols = mu[0].size
        val rows = octaves.size

        for (i in 0 until rows) {
            val octave = octaves[i]

            for (j in 0 until cols) {
                if (candidates[i][j]) {
                    mu[i][j] = 1.0 - abs(err[0][j] - octave[0])
                }
            }
        }
        return mu
    }
    /*
    Primes numbers up to a defined limit, with 1 included
     */
    private fun primesWithOne(upto: Int = 1_000_000): IntArray {

        // handle special cases of upto
        if (upto < 1) return intArrayOf()
        if (upto == 1) return intArrayOf(1)
        if (upto == 2) return intArrayOf(2)

        // generate an array of all odd numbers (excluding 1) up to limit
        val primes = IntArray((upto - 1) / 2 ){ it * 2 + 3}
        // boolean mask indicating whether a given array member is prime
        val isPrime = BooleanArray(primes.size) {true}

        // iterate over factors up to square root of limit
        val squareRoot = sqrt(upto.toDouble()).toInt()
        for (i in 0 until (squareRoot -1) / 2){
            val factor = primes[i]
            if (isPrime[(factor - 2) / 2]){
                var j  = (factor * 3 - 2) / 2
                while (j < isPrime.size) {
                    isPrime[j] = false
                    j += factor
                }
            }
        }
        // create result array of all prime numbers + 1 + 2
        val result = ArrayList<Int>(primes.size / 2 + 2)
        result.add(1)
        result.add(2)

        for (i in primes.indices) {
            if (isPrime[i]) {
                result.add(primes[i])
            }
        }

        return result.toIntArray()
    }



    /*
    Helper function to generate a hanning window of size M
    Based on numpy implementation: https://numpy.org/devdocs/reference/generated/numpy.hanning.html

    @param M: Number of points in the output window. If zero or less, an empty array is returned.
    @return periodic Hanning window
     */
    private fun periodicHanningWindow(M: Double): DoubleArray{
        var values = doubleArrayOf(0.0,M)

        if (M < 1.0){
            return doubleArrayOf()
        } else if (M == 1.0){
            return doubleArrayOf(1.0)
        } else {
            val n = DoubleArray(
                size = abs(ceil((M - (1.0-M)) / 2.0)).toInt()
            ){
                    i -> 0.5+0.5*cos(PI*((1-M) + i * 2.0)/(M-1))
            }
            return n.sliceArray(0..(M-1).toInt())
        }
    }

    /*
    Helper function to calculate rfftFrequencies based on numpy.fft.rfftFreq
    Source: https://numpy.org/devdocs/reference/generated/numpy.fft.rfftfreq.html
     */
    private fun rfft(
        windowSize: Int,
        sampleRate: Double
    ): DoubleArray {
        require(windowSize > 0)
        require(sampleRate > 0.0)

        // np.fft.rfftfreq(windowSize, d = 1 / sampleRate)
        return DoubleArray(windowSize / 2 + 1) { k ->
            k * sampleRate / windowSize
        }
    }

    /*
        Logic to calculate X_erb structure used in python implementation
        Resultant structure is a 3d array representing FFT per channel per window size
     */
    private fun generateXerb(): Array<Array<DoubleArray>>{
        // place holder 3d array to store FFT per channel per windowsize
        val X_erb = mutableListOf<Array<DoubleArray>>()

        // for every window size, generate FFT (resampled to ERB) for every channel
        // and add to outer array
        for (window_idx in windowSizes.indices){
            val N = windowSizes[window_idx]
            val windowDelay = max(N/ 2,delaySize)
            // get segment from buffer and apply window
            val start = windowDelay - N / 2
            val window = windows[window_idx]
            val xWindowed = Array(numChannels) { channel ->
                DoubleArray(N) { i ->
                    buffer[start + i][channel] * window[i]
                }
            }

            /*
            This code generates the variable X in the python implementation
            X is a 2d array in which each row is the FFT for a single channel
            It should have shape (num_channels, N / 2 + 1) where N = windows size
             */

            // select preinitialized fft object for window size
            val fft = ffts[window_idx] // fft implementation from JTransforms

            // container for fft values
            val X = Array(numChannels) { DoubleArray(N / 2 + 1) }

            // compute fft values per channel
            for (channel in 0 until numChannels) {
                // create a copy to perform FFT in place
                val signal = xWindowed[channel].clone()
                fft.realForward(signal)

                // Compute magnitudes
                X[channel][0] = abs(signal[0])

                for (k in 1 until N / 2) {
                    // separate real and imaginary components of FFT (see JTransforms documentation)
                    val real = signal[2 * k]
                    val imaginary = signal[2 * k + 1]
                    X[channel][k] = hypot(real, imaginary)
                }
                X[channel][N / 2] = abs(signal[1])
            }

            // resample X to erb scale
            // results in a 2d array of shape (numChannels, erbFreqs.size)
            // that is to say, the fft for each channel resamples to ERB frequencies
            // for channel in channels

            val erb_resampled = Array(numChannels){
                i -> interpolator.linearInterpolation(erbFreqs,fftFreqs[window_idx],X[i])
            }
            // warp each val by sqrt
            for (row in erb_resampled.indices){
                for (col in erb_resampled[row].indices){
                    erb_resampled[row][col] = sqrt(max(erb_resampled[row][col],0.0))
                }
            }
            X_erb.add(erb_resampled)
        }
        // reshape X_erb from (windows * channels * erb values) ->
        // (channels * windows * erb values)
        val X_erb_reshaped = Array(X_erb[0].size){
            i -> Array(X_erb.size){
                j -> X_erb[j][i].copyOf()
            }
        }

        return X_erb_reshaped
    }

    /*
    Correlate spectra with kernels

    @param spectra as calculated by generateXerb
    @param kernels
    */
    private fun correlateSpectra(
        spectra: Array<Array<DoubleArray>>,
        kernels: Array<DoubleArray>
    ): Array<Array<DoubleArray>>{

        val channels = spectra.size
        val windows = spectra[0].size
        val erbVals = spectra[0][0].size
        val numOutputs = kernels.size

        require(kernels.all { it.size == erbVals })

        val result = Array(channels) {
            Array(windows) {
                DoubleArray(numOutputs)
            }
        }

        for (channel in 0 until channels) {
            val channelSpectra = spectra[channel]
            val channelResult = result[channel]

            for (window in 0 until windows) {
                val spectrum = channelSpectra[window]
                val output = channelResult[window]

                for (out in 0 until numOutputs) {
                    val kernel = kernels[out]
                    var sum = 0.0

                    for (erb in 0 until erbVals) {
                        sum += spectrum[erb] * kernel[erb]
                    }

                    output[out] = sum
                }
            }
        }
        return result
    }

    /*
    Merge pitch strengths computed for different window sizes
    (essentially flattening the window dimension)
     */
    fun mergePitchStrengths(S_all: Array<Array<DoubleArray>>): Array<DoubleArray>{
        val channels = S_all.size
        val windows = S_all[0].size
        val n_pitchCandidates = S_all[0][0].size

        // some checks for dimensionality
        require(mu.size == windows)
        require(mu.all { it.size == n_pitchCandidates })

        return Array(channels) { channel ->
            DoubleArray(n_pitchCandidates) { pitch ->
                var sum = 0.0

                for (window in 0 until windows) {
                    sum +=
                        S_all[channel][window][pitch] *
                                mu[window][pitch]
                }

                sum
            }
        }
    }
    /*
    Transpose a 2d matrix

    @param matrix
    @return transposed matrix
     */
    fun transpose2d(matrix: Array<DoubleArray>): Array<DoubleArray>{
        val rows = matrix.size
        val cols = matrix[0].size

        return Array(cols) { col ->
            DoubleArray(rows) { row ->
                matrix[row][col]
            }
        }
    }

    /*
    Resets class variables modified during use to allow same object to be used repeatedly
     */
    override fun reset(){
        // reset buffer
        buffer = Array(windowSizes.max()){
                i -> DoubleArray(numChannels){
                j -> 0.0
            }
        }
    }

}



/*
Helper class to perform interpolation (parabolic + linear)
 */
class Interpolator(){
    /*
    Parabolic interpolation between pitch candidates using pitch strength

    @param pitchStrengths 2d array where pitchStrengths[i][j] represents strength of pitchCanddiates[i] on channel [j]
    @param strengthThreshold
    @param pitchCandidates
    @return result of pitch interpolation
    */
    fun parabolicInterpolation(
        pitchStrengths: Array<DoubleArray>,
        strengthThreshold: Double,
        pitchCandidates: DoubleArray // evenly spaced pitch candidates as defined in init
    ): PitchInterpolationResult {

        /*
        Helper function to find the peak for a given channel
        Note: to be consistent with the original implementation
        we select the FIRST instance of the peak if there are ties
         */
        fun peakIdx(pitchStrengths: Array<DoubleArray>, channelIdx: Int): Int {
            var peakIdx = 0
            var maxStrength = pitchStrengths[0][channelIdx]

            for (pitchIdx in pitchStrengths.indices) {
                var pitchStrength = pitchStrengths[pitchIdx][channelIdx]
                if (pitchStrength > maxStrength){
                    peakIdx = pitchIdx
                    maxStrength = pitchStrength
                }
            }
            return peakIdx
        }


        val n_channels = pitchStrengths[0].size
        val pitches = DoubleArray(n_channels) {0.0}
        val strengths = DoubleArray(n_channels) {0.0}


        for (channelIdx in 0 until n_channels) {
            // find the index of the strongest pitch for each channel
            var peakIdx = peakIdx(pitchStrengths, channelIdx)
            // record strongest value for channel
            strengths[channelIdx] = pitchStrengths[peakIdx][channelIdx]
            // ignore strengths lower than threshold
            if (strengths[channelIdx] < strengthThreshold){
                continue
            }
            // ignore indexes at boundaries
            if (peakIdx == 0 || peakIdx == pitchCandidates.size - 1){
                pitches[channelIdx] = pitchCandidates[peakIdx]
                continue
            }
            // conduct interpolation for channel
            interpolateChannel(
                pitchStrengths,
                pitchCandidates,
                channelIdx,
                peakIdx,
                pitches,
                strengths
            )
        }
        return PitchInterpolationResult(
            pitches = pitches,
            strengths = strengths
        )
    }

    /*
    Helper function interpolate a single channel's strengths
    */
    private fun interpolateChannel(
        pitchStrengths: Array<DoubleArray>,
        pitchCandidates: DoubleArray,
        channelIdx: Int,
        peakIdx: Int,
        pitches: DoubleArray,
        strengths: DoubleArray
    ){
        // equivalent to max_indices = np.arange(index_of_largest - 1, index_of_largest + 2)
        val left = peakIdx - 1
        val centre = peakIdx
        val right = peakIdx + 1

        val peakCandidates = doubleArrayOf(
            pitchStrengths[left][channelIdx],
            pitchStrengths[centre][channelIdx],
            pitchStrengths[right][channelIdx]
        )
        // abandon interpolation if any values are nan
        if (peakCandidates.any({it.isNaN()})){
            strengths[channelIdx] = Double.NaN
            pitches[channelIdx] = Double.NaN
            return
        }

        val normalize: (Double) -> Double = {
            x -> (x /(1.0/pitchCandidates[centre]) -1 )*(2.0*PI)
        }

        val ntc = doubleArrayOf(
            normalize(1/pitchCandidates[left]),
            normalize(1/pitchCandidates[centre]),
            normalize(1/pitchCandidates[right])
        )
        // find the coefficients of the 3 point Lagrange interpolation of the points (ntc[i], peakCandidates[i])
        val coefficients = threePointLagrangePolynomial(ntc, peakCandidates)

        // populate ftc
        var maxStrength = Double.NEGATIVE_INFINITY
        var k = 0
        val start = log2(pitchCandidates[left])
        val stop = log2(pitchCandidates[right])

        var curr = start
        var currIdx = 0
        val step = 1.0 / 12.0 / 64.0

        while (curr < stop) {

            val ftc = 1.0 / 2.0.pow(curr)

            val nftc =
                (ftc / (1/pitchCandidates[centre]) - 1.0) * 2.0 * PI

            val interpolatedStrength = evaluateQuadratic(
                a = coefficients[0],
                b = coefficients[1],
                c = coefficients[2],
                x = nftc
            )

            if (interpolatedStrength > maxStrength) {
                maxStrength = interpolatedStrength
                k = currIdx
            }

            currIdx++
            curr = start + currIdx * step
        }

        strengths[channelIdx] = maxStrength
        pitches[channelIdx] = 2.0.pow(start + k / 12.0 / 64.0)
    }

    /*
    Fit a polynomial between 3 points (x0,y0), (x1,y1), (x2,y2), returning coefficients a b c

    Based on the general formula found here: https://brilliant.org/wiki/lagrange-interpolation/
     */
    private fun threePointLagrangePolynomial(
        xCoords: DoubleArray,
        yCoords: DoubleArray
    ): DoubleArray {
        require(xCoords.size == 3 && yCoords.size == 3){
            "This implementation only works for 3 points"
        }
        val x0 = xCoords[0]
        val x1 = xCoords[1]
        val x2 = xCoords[2]

        val y0 = yCoords[0]
        val y1 = yCoords[1]
        val y2 = yCoords[2]

        // denominators of lagrange formula
        val d0 = (x0-x1)*(x0-x2)
        val d1 = (x1-x0)*(x1-x2)
        val d2 = (x2-x0)*(x2-x1)

        val a =
            y0 / d0 +
                    y1 / d1 +
                    y2 / d2

        val b =
            -y0 * (x1 + x2) / d0 -
                    y1 * (x0 + x2) / d1 -
                    y2 * (x0 + x1) / d2

        val c =
            y0 * x1 * x2 / d0 +
                    y1 * x0 * x2 / d1 +
                    y2 * x0 * x1 / d2

        return doubleArrayOf(a, b, c)
    }

    /*
    Evaluate quadratic function using Horner's method.
    https://www.geeksforgeeks.org/dsa/horners-method-polynomial-evaluation/
     */
    private fun evaluateQuadratic(
        a: Double, // a, b, c coefficients in ax^2 + bx + c
        b: Double,
        c: Double,
        x: Double
    ): Double{
        return (a * x + b) * x + c
    }

    /*
    Linear interpolation based on numpy.interp
    Docs: https://numpy.org/doc/stable/reference/generated/numpy.interp.html

    @param x The x-coordinates at which to evaluate the interpolated values.
    @param xp The x-coordinates of the data points
    @param fp The y-coordinates of the data points, same length as xp.
     */
    fun linearInterpolation(
        x: DoubleArray,
        xp: DoubleArray,
        fp: DoubleArray
    ): DoubleArray {
        require(xp.size == fp.size){
            "LinearInterpolation failed: fp and xp are not of the same length"
        }
        val result = DoubleArray(x.size)

        for (i in x.indices) {
            val x_val = x[i]

            // Left extrapolation
            if (x_val <= xp.first()) {
                result[i] = fp.first()
                continue
            }

            // Right extrapolation
            if (x_val >= xp.last()) {
                result[i] = fp.last()
                continue
            }

            // Binary search
            var low = 0
            var high = xp.lastIndex

            while (high - low > 1) {
                val mid = (low + high) / 2
                if (xp[mid] <= x_val)
                    low = mid
                else
                    high = mid
            }

            // Linear interpolation
            val x0 = xp[low]
            val x1 = xp[high]
            val y0 = fp[low]
            val y1 = fp[high]

            result[i] = y0 + (x_val - x0) * (y1 - y0) / (x1 - x0)
        }

        return result

    }
}

/*
Data class to store results of parabolic interpolation.
 */
data class PitchInterpolationResult(
    val pitches: DoubleArray,
    val strengths: DoubleArray
)


