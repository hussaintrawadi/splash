package com.splash.water

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splash.water.domain.model.ThemeMode
import com.splash.water.ui.AppViewModel
import com.splash.water.ui.navigation.SplashRoot
import com.splash.water.ui.onboarding.OnboardingFlow
import com.splash.water.ui.theme.SplashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val state by appViewModel.state.collectAsStateWithLifecycle()

            val dark = when (state.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            SplashTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        state.loading -> Unit
                        !state.onboardingComplete -> OnboardingFlow(onDone = {})
                        else -> SplashRoot()
                    }
                }
            }
        }
    }
}
