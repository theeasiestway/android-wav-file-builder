package com.theeasiestway.wavformat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val SAMPLE_RATE = 44100

    private lateinit var vStartRec: Button
    private lateinit var vSaveRec: Button
    private val voiceRecorder = VoiceRecorder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vStartRec = findViewById(R.id.vStartRec)
        vStartRec.setOnClickListener {
            onRecClicked()
            checkRecPermission()
        }
        vSaveRec = findViewById(R.id.vSaveRec)
        vSaveRec.setOnClickListener {
            onSaveRecClicked()
            checkWritePermission()
        }
    }

    private fun onRecClicked() {
        vStartRec.visibility = View.GONE
        vSaveRec.visibility = View.VISIBLE
    }

    private fun onSaveRecClicked() {
        vSaveRec.visibility = View.GONE
        vStartRec.visibility = View.VISIBLE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (permissions[0] == Manifest.permission.RECORD_AUDIO) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startRecord()
                else {
                    onSaveRecClicked()
                    showToast("Record audio permission is denied")
                }
            }
            if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) requestReadPermission()
                else {
                    onSaveRecClicked()
                    showToast("Write external storage permission is denied")
                }
            }
            if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) saveRecord()
                else {
                    onSaveRecClicked()
                    showToast("Read external storage permission is denied")
                }
            }
        }
    }

    private fun checkRecPermission() {
        if (checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) requestAudioPermission()
        else startRecord()
    }

    private fun checkWritePermission() {
        if (checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) requestWritePermission()
        else saveRecord()
    }

    private fun startRecord() {
        voiceRecorder.prepare(SAMPLE_RATE, 320).start()
    }

    private fun saveRecord() {
        val wavFile = WavFileBuilderKotlin()
            .setAudioFormat(WavFileBuilderKotlin.PCM_AUDIO_FORMAT)
            .setSampleRate(SAMPLE_RATE)
            .setBitsPerSample(WavFileBuilderKotlin.BITS_PER_SAMPLE_16)
            .setNumChannels(WavFileBuilderKotlin.CHANNELS_MONO)
            .setSubChunk1Size(WavFileBuilderKotlin.SUBCHUNK_1_SIZE_PCM)
            .build(voiceRecorder.stop())

        val rootFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(rootFolder, "audio_rec.wav")
        if (!file.exists()) file.createNewFile()

        val fos = FileOutputStream(file)
        fos.write(wavFile)
        fos.flush()
        fos.close()

        showToast("Wav file successfully saved in: ${file.absolutePath}")
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
    }

    private fun requestWritePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    }

    private fun requestReadPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
    }
}