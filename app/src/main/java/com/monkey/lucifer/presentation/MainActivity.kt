/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.monkey.lucifer.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.monkey.lucifer.presentation.theme.LuciferTheme

class MainActivity : ComponentActivity() {
    private val homeViewModel by lazy { ViewModelProvider(this)[HomeViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Initialize ViewModel BEFORE setting content
        homeViewModel.initialize(this)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(viewModel = homeViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume - triggering auto-start")
        homeViewModel.resetForAutoStart()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onKeyDown: keyCode=$keyCode, event=$event")
        if (event?.repeatCount == 0 && isPttKey(keyCode)) {
            Log.d("MainActivity", "PTT Key pressed - starting recording")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                homeViewModel.startRecording(this)
            } else {
                Log.e("MainActivity", "Microphone permission not granted")
                homeViewModel.stopRecordingAndProcess()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onKeyUp: keyCode=$keyCode, event=$event")
        if (isPttKey(keyCode)) {
            Log.d("MainActivity", "PTT Key released - stopping recording")
            homeViewModel.stopRecordingAndProcess()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun isPttKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_STEM_PRIMARY ||
            keyCode == KeyEvent.KEYCODE_STEM_1 ||
            keyCode == KeyEvent.KEYCODE_BACK ||
            keyCode == KeyEvent.KEYCODE_MENU
    }
}

@Composable
fun WearApp(viewModel: HomeViewModel) {
    LuciferTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            HomePage(viewModel = viewModel)
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    // Preview with a mock view model - won't work in preview but allows compilation
    LuciferTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
        }
    }
}