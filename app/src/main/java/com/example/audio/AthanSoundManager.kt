package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.models.AthanVoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class AthanSoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var activePlayJob: Job? = null
    private var currentAudioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun isCurrentlyPlaying(): Boolean = isPlaying

    fun stopAudio() {
        activePlayJob?.cancel()
        activePlayJob = null
        try {
            currentAudioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
            currentAudioTrack = null
        } catch (e: Exception) {
            Log.e("AthanSoundManager", "Error stopping AudioTrack: ${e.message}")
        }

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AthanSoundManager", "Error stopping MediaPlayer: ${e.message}")
        }
        isPlaying = false
    }

    /**
     * Play Athan notification tone / synthesized adhan melody
     */
    fun playAthan(
        voice: AthanVoice,
        volumePercent: Int = 100,
        onComplete: (() -> Unit)? = null
    ) {
        if (voice == AthanVoice.SILENT) {
            onComplete?.invoke()
            return
        }

        stopAudio()
        isPlaying = true

        activePlayJob = scope.launch {
            try {
                if (voice == AthanVoice.BEEP_ONLY) {
                    playTripleChime(volumePercent)
                } else {
                    playSynthesizedAdhanSequence(voice, volumePercent)
                }
            } catch (e: Exception) {
                Log.e("AthanSoundManager", "Playback error: ${e.message}")
            } finally {
                isPlaying = false
                Handler(Looper.getMainLooper()).post {
                    onComplete?.invoke()
                }
            }
        }
    }

    /**
     * Play short Iqama alert chime (double gong/takbeer)
     */
    fun playIqamaAlert(volumePercent: Int = 100) {
        stopAudio()
        isPlaying = true
        activePlayJob = scope.launch {
            try {
                val sampleRate = 44100
                val freqList = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
                for (freq in freqList) {
                    if (!isActive) break
                    generateTone(freq, 280, volumePercent, sampleRate)
                    delay(120)
                }
            } catch (e: Exception) {
                Log.e("AthanSoundManager", "Iqama chime error: ${e.message}")
            } finally {
                isPlaying = false
            }
        }
    }

    /**
     * Short triple chime for tests or reminders
     */
    private suspend fun playTripleChime(volumePercent: Int) {
        val sampleRate = 44100
        val notes = listOf(440.0, 554.37, 659.25, 880.0)
        for (freq in notes) {
            if (!scope.isActive) break
            generateTone(freq, 350, volumePercent, sampleRate)
            delay(150)
        }
    }

    /**
     * Plays a high-fidelity harmonic synthesized Islamic Adhan Maqam melody (Bayati / Rast)
     * Compatible with any device without external internet asset downloads.
     */
    private suspend fun playSynthesizedAdhanSequence(voice: AthanVoice, volumePercent: Int) {
        val sampleRate = 44100

        // Frequencies corresponding to traditional Maqam Bayati / Hijaz intervals for "Allahu Akbar" and Athan lines
        val baseFreq = when (voice) {
            AthanVoice.BAGHDAD -> 220.0 // Iraqi Maqam A3
            AthanVoice.MAKKAH -> 246.94 // B3
            AthanVoice.MADINAH -> 261.63 // C4
            AthanVoice.AL_AQSA -> 233.08 // Bb3
            else -> 220.0
        }

        // 1. Allahu Akbar, Allahu Akbar (Phrase 1)
        val phrase1 = listOf(
            Pair(baseFreq * 1.0, 600),
            Pair(baseFreq * 1.125, 500),
            Pair(baseFreq * 1.25, 900),
            Pair(baseFreq * 1.125, 600),
            Pair(baseFreq * 1.0, 1100)
        )

        // 2. Allahu Akbar, Allahu Akbar (Phrase 2 higher)
        val phrase2 = listOf(
            Pair(baseFreq * 1.25, 600),
            Pair(baseFreq * 1.334, 500),
            Pair(baseFreq * 1.5, 1100),
            Pair(baseFreq * 1.334, 600),
            Pair(baseFreq * 1.25, 1200)
        )

        // 3. Ash-hadu alla ilaha illallah
        val phrase3 = listOf(
            Pair(baseFreq * 1.0, 700),
            Pair(baseFreq * 1.125, 600),
            Pair(baseFreq * 1.25, 700),
            Pair(baseFreq * 1.334, 900),
            Pair(baseFreq * 1.125, 600),
            Pair(baseFreq * 1.0, 1400)
        )

        // 4. Hayya 'ala-s-Salah
        val phrase4 = listOf(
            Pair(baseFreq * 1.5, 800),
            Pair(baseFreq * 1.667, 700),
            Pair(baseFreq * 1.5, 900),
            Pair(baseFreq * 1.334, 700),
            Pair(baseFreq * 1.25, 1300)
        )

        val fullMelody = phrase1 + listOf(Pair(0.0, 300)) +
                phrase2 + listOf(Pair(0.0, 400)) +
                phrase3 + listOf(Pair(0.0, 400)) +
                phrase4

        for ((freq, durationMs) in fullMelody) {
            if (!scope.isActive) break
            if (freq == 0.0) {
                delay(durationMs.toLong())
            } else {
                generateTone(freq, durationMs, volumePercent, sampleRate)
                delay(30)
            }
        }
    }

    private fun generateTone(freq: Double, durationMs: Int, volumePercent: Int, sampleRate: Int) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val generatedSnd = ShortArray(numSamples)
        val volFactor = (volumePercent.coerceIn(0, 100) / 100.0) * 0.85

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples

            // Attack - Decay - Sustain - Release envelope
            val envelope = when {
                progress < 0.08 -> progress / 0.08
                progress > 0.80 -> (1.0 - progress) / 0.20
                else -> 1.0
            }

            // Rich warm harmonic overtone series (Fundamental + 2nd + 3rd harmonic)
            val fundamental = sin(2 * PI * freq * t)
            val harmonic2 = 0.45 * sin(2 * PI * (freq * 2) * t)
            val harmonic3 = 0.25 * sin(2 * PI * (freq * 3) * t)
            val vibrato = 1.0 + 0.03 * sin(2 * PI * 5.0 * t)

            val sampleVal = (fundamental + harmonic2 + harmonic3) * envelope * volFactor * vibrato
            generatedSnd[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }

        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .build()

            val track = AudioTrack(
                audioAttributes,
                audioFormat,
                generatedSnd.size * 2,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            track.write(generatedSnd, 0, generatedSnd.size)
            track.play()
            currentAudioTrack = track

            Thread.sleep(durationMs.toLong())
            track.stop()
            track.release()
            currentAudioTrack = null
        } catch (e: Exception) {
            Log.e("AthanSoundManager", "Error in generateTone: ${e.message}")
        }
    }
}
