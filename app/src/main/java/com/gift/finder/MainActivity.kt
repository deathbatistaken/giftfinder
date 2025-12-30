package com.gift.finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
class MainActivity : ComponentActivity() {
    
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val mainViewModel: com.gift.finder.ui.viewmodels.MainViewModel = androidx.lifecycle.viewmodel.compose.hiltViewModel()
            val aura by mainViewModel.cosmicAura.collectAsState(initial = com.gift.finder.domain.model.CosmicAura.NEBULA)
            
            GiftFinderTheme(aura = aura) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GiftFinderNavHost(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}
