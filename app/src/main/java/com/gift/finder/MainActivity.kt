package com.gift.finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gift.finder.ui.navigation.GiftFinderNavHost
import com.gift.finder.ui.theme.GiftFinderTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

/**
 * Main Activity - Single Activity architecture with Compose navigation.
 */
@AndroidEntryPoint
class MainActivity : androidx.appcompat.app.AppCompatActivity() {
    
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val mainViewModel: com.gift.finder.ui.viewmodels.MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            
            val aura by mainViewModel.cosmicAura.collectAsState(initial = com.gift.finder.domain.model.CosmicAura.NEBULA)
            val themeMode by mainViewModel.themeMode.collectAsState(initial = "system")
            val appLanguage by mainViewModel.appLanguage.collectAsState(initial = "en")
            
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            // Sync theme with AppCompatDelegate
            LaunchedEffect(themeMode) {
                val mode = when (themeMode) {
                    "light" -> AppCompatDelegate.MODE_NIGHT_NO
                    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(mode)
            }
            
            // Sync language with AppCompatDelegate
            LaunchedEffect(appLanguage) {
                val appLocales = LocaleListCompat.forLanguageTags(appLanguage)
                AppCompatDelegate.setApplicationLocales(appLocales)
            }
            
            GiftFinderTheme(
                darkTheme = isDarkTheme,
                aura = aura
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GiftFinderNavHost(windowSizeClass = windowSizeClass)
                }
            }
            
            // Handle Deep Link
            val intent = intent
            if (intent?.action == "com.gift.finder.ACTION_ADD_PERSON") {
                mainViewModel.setDeepLink("add_person")
                intent.action = null // Consume
            }
        }
    }
}
