package com.theeasiestway.wavformat;

import java.util.Arrays;

public class WavFileBuilderJava {

    /**
     * The wave format consist of two sub-chunks, the first one is "fmt " and the second is "data".
     * The "fmt " sub-chunk describes the format of the sound information in the "data" sub-chunk.
     * The "data" sub-chunk indicates the size of the sound information and contains the raw sound data.
     */
    public static final int SAMPLE_RATE_8000 = 8000;
    public static final int BITS_PER_SAMPLE_16 = 16;
    public static final int BITS_PER_SAMPLE_8 = 8;
    public static final int CHANNELS_MONO = 1;
    public static final int CHANNELS_STEREO = 2;
    public static final int SUBCHUNK_1_SIZE_PCM = 16;
    public static final int PCM_AUDIO_FORMAT = 1;
    private final int HEADER_SIZE = 44;

    private int channels = -1;
    private int bitsPerSample = -1;
    private int sampleRate = -1;
    private byte[] header;

    public WavFileBuilderJava() {

        this.header = new byte[HEADER_SIZE];
    }

    private void setConstants() {

        /** chunkId - contains the letters "RIFF" in ASCII form */

        this.header[0] = 'R';
        this.header[1] = 'I';
        this.header[2] = 'F';
        this.header[3] = 'F';

        /** format - contains the letters "WAVE" */

        this.header[8]  = 'W';
        this.header[9]  = 'A';
        this.header[10] = 'V';
        this.header[11] = 'E';

        /** subchunk1Id - contains the letters "fmt " */

        this.header[12] = 'f';
        this.header[13] = 'm';
        this.header[14] = 't';
        this.header[15] = ' ';

        /** subchunk2Id - contains the letters "data" */

        this.header[36] = 'd';
        this.header[37] = 'a';
        this.header[38] = 't';
        this.header[39] = 'a';
    }

    /**
     * 36 + SubChunk2Size, or more precisely:
     * 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
     * This is the size of the rest of the chunk
     * following this number.  This is the size of the
     * entire file in bytes minus 8 bytes for the
     * two fields not included in this count: ChunkID and ChunkSize.
     */

    private void setChunkSize(int samplesCount) {
        int size = 36 + samplesCount;
        this.header[4] = (byte) (size & 0xFF);
        this.header[5] = (byte) ((size >> 8) & 0xFF);
        this.header[6] = (byte) ((size >> 16) & 0xFF);
        this.header[7] = (byte) ((size >> 24) & 0xFF);
    }

    /** 16 for PCM. This is the size of the rest of the Subchunk which follows this number. */

    public WavFileBuilderJava setSubChunk1Size(int size) {
        this.header[16] = (byte) (size & 0xFF);
        this.header[17] = (byte) ((size >> 8) & 0xFF);
        this.header[18] = (byte) ((size >> 16) & 0xFF);
        this.header[19] = (byte) ((size >> 24) & 0xFF);
        return this;
    }

    /** PCM = 1 (i.e. Linear quantization) Values other than 1 indicate some form of compression. */

    public WavFileBuilderJava setAudioFormat(int format) {
        this.header[20] = (byte) (format & 0xFF);
        this.header[21] = (byte) ((format >> 8) & 0xFF);
        return this;
    }

    /** Number of channels. Mono = 1, Stereo = 2, etc. */

    public WavFileBuilderJava setNumChannels(int channels) {

        this.channels = channels;

        this.header[22] = (byte) (channels & 0xFF);
        this.header[23] = (byte) ((channels >> 8) & 0xFF);

        return this;
    }

    /** Sample rate 8000, 44100, etc. */

    public WavFileBuilderJava setSampleRate(int sampleRate) {

        this.sampleRate = sampleRate;

        this.header[24] = (byte) (sampleRate & 0xFF);
        this.header[25] = (byte) ((sampleRate >> 8) & 0xFF);
        this.header[26] = (byte) ((sampleRate >> 16) & 0xFF);
        this.header[27] = (byte) ((sampleRate >> 24) & 0xFF);

        return this;
    }

    /** sampleRate * numChannels * bitsPerSample / 8 */

    private void setByteRate() {

        if (sampleRate == -1) throw new IllegalArgumentException("The sample rate is not specified");

        if (channels == -1) throw new IllegalArgumentException("The number of channels is not specified");

        if (bitsPerSample == -1) throw new IllegalArgumentException("The bits per a sample is not specified");

        int byteRate = sampleRate * channels * bitsPerSample / 8;

        this.header[28] = (byte) (byteRate & 0xFF);
        this.header[29] = (byte) ((byteRate >> 8) & 0xFF);
        this.header[30] = (byte) ((byteRate >> 16) & 0xFF);
        this.header[31] = (byte) ((byteRate >> 24) & 0xFF);
    }

    /**
     * numChannels * bitsPerSample / 8
     * The number of bytes for one sample including
     * all channels. I wonder what happens when
     * this number isn't an integer?
     */

    private void setBlockAlign() {

        if (channels == -1) throw new IllegalArgumentException("The number of channels is not specified");

        if (bitsPerSample == -1) throw new IllegalArgumentException("The bits per a sample is not specified");

        int byteCount = (channels * bitsPerSample / 8);

        this.header[32] = (byte) (byteCount & 0xFF);
        this.header[33] = (byte) ((byteCount >> 8) & 0xFF);
    }

    /** 8 bits = 8, 16 bits = 16, etc. */

    public WavFileBuilderJava setBitsPerSample(int bitsPerSample) {

        this.bitsPerSample = bitsPerSample;

        this.header[34] = (byte) (bitsPerSample & 0xFF);
        this.header[35] = (byte) ((bitsPerSample >> 8) & 0xFF);

        return this;
    }

    /**
     * NumSamples * NumChannels * BitsPerSample / 8
     * This is the number of bytes in the data.
     * You can also think of this as the size
     * of the read of the subchunk following this number
     */

    private void setSubChunk2Size(int size) {

        this.header[40] = (byte) (size & 0xFF);
        this.header[41] = (byte) ((size >> 8) & 0xFF);
        this.header[42] = (byte) ((size >> 16) & 0xFF);
        this.header[43] = (byte) ((size >> 24) & 0xFF);
    }

    /** returns only a header of the wav file  */

    public byte[] buildHeader(int samplesCount) {

        setConstants();

        setByteRate();

        setBlockAlign();

        setSubChunk2Size(samplesCount);

        setChunkSize(samplesCount);

        return header;
    }

    /** returns a completed wav file (header + audio data) */

    public byte[] build(byte[] data) {

        if (data != null) {

            int dataLength = data.length;

            buildHeader(dataLength);

            byte[] wavBytes = new byte[dataLength + HEADER_SIZE];

            System.arraycopy(header, 0, wavBytes, 0, HEADER_SIZE);

            System.arraycopy(data, 0, wavBytes, HEADER_SIZE, dataLength);

            clear();

            return wavBytes;
        }

        return null;
    }

    /** clears all the variables */

    private void clear() {

        Arrays.fill(header, (byte) 0);

        this.channels = -1;
        this.bitsPerSample = -1;
        this.sampleRate = -1;
    }
}
