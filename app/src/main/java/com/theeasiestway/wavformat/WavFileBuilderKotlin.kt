package com.theeasiestway.wavformat

import java.util.Arrays

class WavFileBuilderKotlin {

    companion object {

        /**
         * The wave format consist of two sub-chunks, the first one is "fmt " and the second is "data".
         * The "fmt " sub-chunk describes the format of the sound information in the "data" sub-chunk.
         * The "data" sub-chunk indicates the size of the sound information and contains the raw sound data.
         */
        val SAMPLE_RATE_8000 = 8000
        val BITS_PER_SAMPLE_16 = 16
        val BITS_PER_SAMPLE_8 = 8
        val CHANNELS_MONO = 1
        val CHANNELS_STEREO = 2
        val SUBCHUNK_1_SIZE_PCM = 16
        val PCM_AUDIO_FORMAT = 1
    }

    private val HEADER_SIZE = 44

    private var channels = -1
    private var bitsPerSample = -1
    private var sampleRate = -1
    private val header: ByteArray

    init {

        this.header = ByteArray(HEADER_SIZE)
    }

    private fun setConstants() {

        /** chunkId - contains the letters "RIFF" in ASCII form  */

        this.header[0] = 'R'.toByte()
        this.header[1] = 'I'.toByte()
        this.header[2] = 'F'.toByte()
        this.header[3] = 'F'.toByte()

        /** format - contains the letters "WAVE"  */

        this.header[8] = 'W'.toByte()
        this.header[9] = 'A'.toByte()
        this.header[10] = 'V'.toByte()
        this.header[11] = 'E'.toByte()

        /** subchunk1Id - contains the letters "fmt "  */

        this.header[12] = 'f'.toByte()
        this.header[13] = 'm'.toByte()
        this.header[14] = 't'.toByte()
        this.header[15] = ' '.toByte()

        /** subchunk2Id - contains the letters "data"  */

        this.header[36] = 'd'.toByte()
        this.header[37] = 'a'.toByte()
        this.header[38] = 't'.toByte()
        this.header[39] = 'a'.toByte()
    }

    /**
     * 36 + SubChunk2Size, or more precisely:
     * 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
     * This is the size of the rest of the chunk
     * following this number.  This is the size of the
     * entire file in bytes minus 8 bytes for the
     * two fields not included in this count: ChunkID and ChunkSize.
     */

    private fun setChunkSize(samplesCount: Int) {
        val size = 36 + samplesCount
        this.header[4] = (size and 0xFF).toByte()
        this.header[5] = (size shr 8 and 0xFF).toByte()
        this.header[6] = (size shr 16 and 0xFF).toByte()
        this.header[7] = (size shr 24 and 0xFF).toByte()
    }

    /** 16 for PCM. This is the size of the rest of the Subchunk which follows this number.  */

    fun setSubChunk1Size(size: Int): WavFileBuilderKotlin {
        this.header[16] = (size and 0xFF).toByte()
        this.header[17] = (size shr 8 and 0xFF).toByte()
        this.header[18] = (size shr 16 and 0xFF).toByte()
        this.header[19] = (size shr 24 and 0xFF).toByte()
        return this
    }

    /** PCM = 1 (i.e. Linear quantization) Values other than 1 indicate some form of compression.  */

    fun setAudioFormat(format: Int): WavFileBuilderKotlin {
        this.header[20] = (format and 0xFF).toByte()
        this.header[21] = (format shr 8 and 0xFF).toByte()
        return this
    }

    /** Number of channels. Mono = 1, Stereo = 2, etc.  */

    fun setNumChannels(channels: Int): WavFileBuilderKotlin {

        this.channels = channels

        this.header[22] = (channels and 0xFF).toByte()
        this.header[23] = (channels shr 8 and 0xFF).toByte()

        return this
    }

    /** Sample rate 8000, 44100, etc.  */

    fun setSampleRate(sampleRate: Int): WavFileBuilderKotlin {

        this.sampleRate = sampleRate

        this.header[24] = (sampleRate and 0xFF).toByte()
        this.header[25] = (sampleRate shr 8 and 0xFF).toByte()
        this.header[26] = (sampleRate shr 16 and 0xFF).toByte()
        this.header[27] = (sampleRate shr 24 and 0xFF).toByte()

        return this
    }

    /** sampleRate * numChannels * bitsPerSample / 8  */

    private fun setByteRate() {

        if (sampleRate == -1) throw IllegalArgumentException("The sample rate is not specified")

        if (channels == -1) throw IllegalArgumentException("The number of channels is not specified")

        if (bitsPerSample == -1) throw IllegalArgumentException("The bits per a sample is not specified")

        val byteRate = sampleRate * channels * bitsPerSample / 8

        this.header[28] = (byteRate and 0xFF).toByte()
        this.header[29] = (byteRate shr 8 and 0xFF).toByte()
        this.header[30] = (byteRate shr 16 and 0xFF).toByte()
        this.header[31] = (byteRate shr 24 and 0xFF).toByte()
    }

    /**
     * numChannels * bitsPerSample / 8
     * The number of bytes for one sample including
     * all channels. I wonder what happens when
     * this number isn't an integer?
     */

    private fun setBlockAlign() {

        if (channels == -1) throw IllegalArgumentException("The number of channels is not specified")

        if (bitsPerSample == -1) throw IllegalArgumentException("The bits per a sample is not specified")

        val byteCount = channels * bitsPerSample / 8

        this.header[32] = (byteCount and 0xFF).toByte()
        this.header[33] = (byteCount shr 8 and 0xFF).toByte()
    }

    /** 8 bits = 8, 16 bits = 16, etc.  */

    fun setBitsPerSample(bitsPerSample: Int): WavFileBuilderKotlin {

        this.bitsPerSample = bitsPerSample

        this.header[34] = (bitsPerSample and 0xFF).toByte()
        this.header[35] = (bitsPerSample shr 8 and 0xFF).toByte()

        return this
    }

    /**
     * NumSamples * NumChannels * BitsPerSample / 8
     * This is the number of bytes in the data.
     * You can also think of this as the size
     * of the read of the subchunk following this number
     */

    private fun setSubChunk2Size(size: Int) {

        this.header[40] = (size and 0xFF).toByte()
        this.header[41] = (size shr 8 and 0xFF).toByte()
        this.header[42] = (size shr 16 and 0xFF).toByte()
        this.header[43] = (size shr 24 and 0xFF).toByte()
    }

    /** returns only a header of the wav file   */

    fun buildHeader(samplesCount: Int): ByteArray {

        setConstants()

        setByteRate()

        setBlockAlign()

        setSubChunk2Size(samplesCount)

        setChunkSize(samplesCount)

        return header
    }

    /** returns a completed wav file (header + audio data)  */

    fun build(data: ByteArray?): ByteArray? {

        if (data != null) {

            val dataLength = data.size

            buildHeader(dataLength)

            val wavBytes = ByteArray(dataLength + HEADER_SIZE)

            System.arraycopy(header, 0, wavBytes, 0, HEADER_SIZE)

            System.arraycopy(data, 0, wavBytes, HEADER_SIZE, dataLength)

            clear()

            return wavBytes
        }

        return null
    }

    /** clears all the variables  */

    private fun clear() {

        Arrays.fill(header, 0.toByte())

        this.channels = -1
        this.bitsPerSample = -1
        this.sampleRate = -1
    }
}
