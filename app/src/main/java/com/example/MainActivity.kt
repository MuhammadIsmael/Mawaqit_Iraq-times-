package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.audio.AthanSoundManager
import com.example.data.preferences.SettingsRepository
import com.example.ui.screens.MawaqitHomeScreen
import com.example.ui.theme.MawaqitIraqTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var athanSoundManager: AthanSoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on continuously for 24/7 TV Mosque Display operation
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        settingsRepository = SettingsRepository(applicationContext)
        athanSoundManager = AthanSoundManager(applicationContext)

        setContent {
            MawaqitIraqTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MawaqitHomeScreen(
                        settingsRepository = settingsRepository,
                        athanSoundManager = athanSoundManager
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                // Handle remote MENU key to toggle settings
                return true
            }
            KeyEvent.KEYCODE_MEDIA_STOP, KeyEvent.KEYCODE_MUTE -> {
                if (athanSoundManager.isCurrentlyPlaying()) {
                    athanSoundManager.stopAudio()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        athanSoundManager.stopAudio()
    }
}
