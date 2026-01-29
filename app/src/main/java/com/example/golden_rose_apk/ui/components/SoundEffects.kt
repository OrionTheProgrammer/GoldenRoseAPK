package com.example.golden_rose_apk.ui.components

import android.media.AudioManager
import android.media.ToneGenerator

class SoundEffects {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)

    fun playClick() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 120)
    }

    fun playSuccess() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
    }

    fun release() {
        toneGenerator.release()
    }
}
