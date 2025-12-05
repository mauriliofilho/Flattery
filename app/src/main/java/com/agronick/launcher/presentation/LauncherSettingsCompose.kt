package com.agronick.launcher.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.agronick.launcher.data.LauncherPreferences
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.coroutines.launch

/**
 * Modern Compose-based Settings Activity
 * Replaces deprecated PreferenceFragment with Wear OS Compose components
 */
class LauncherSettingsCompose : ComponentActivity() {

    private lateinit var preferences: LauncherPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = LauncherPreferences(applicationContext)

        setContent {
            WearSettingsTheme {
                SettingsScreen(preferences = preferences)
            }
        }
    }
}

@Composable
fun WearSettingsTheme(content: @Composable () -> Unit) {
    // Using Wear Material theme
    androidx.wear.compose.material.MaterialTheme(
        colors = androidx.wear.compose.material.Colors(
            primary = androidx.compose.ui.graphics.Color(0xFF7842F5),
            primaryVariant = androidx.compose.ui.graphics.Color(0xFF808080),
            background = androidx.compose.ui.graphics.Color(0xFF121212),
            surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            onBackground = androidx.compose.ui.graphics.Color(0xFFB1BBC9),
            onSurface = androidx.compose.ui.graphics.Color(0xFFB1BBC9),
            onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF888888),
            error = androidx.compose.ui.graphics.Color(0xFFCF6679)
        ),
        content = content
    )
}

@Composable
fun SettingsScreen(
    preferences: LauncherPreferences,
    modifier: Modifier = Modifier
) {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect preferences as state
    val iconSize by preferences.iconSize.collectAsState(initial = LauncherPreferences.DEFAULT_ICON_SIZE)
    val margin by preferences.margin.collectAsState(initial = LauncherPreferences.DEFAULT_MARGIN)

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        modifier = modifier
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                Text(
                    text = "Configurações",
                    style = androidx.wear.compose.material.MaterialTheme.typography.title3,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                SettingChip(
                    label = "Tamanho dos Ícones: $iconSize",
                    secondaryLabel = "Deslize a coroa para ajustar",
                    onClick = { /* Could open detail screen */ }
                )
            }

            item {
                SettingChip(
                    label = "Espaçamento: $margin",
                    secondaryLabel = "Entre os aplicativos",
                    onClick = { /* Could open detail screen */ }
                )
            }

            item {
                SettingChip(
                    label = "Feedback",
                    secondaryLabel = "Reportar problemas",
                    onClick = {
                        val url = "https://github.com/agronick/Flattery/issues"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                            addCategory(Intent.CATEGORY_BROWSABLE)
                        }
                        RemoteIntent.startRemoteActivity(context, intent, null)
                    }
                )
            }

            item {
                SettingChip(
                    label = "Sobre",
                    secondaryLabel = "Flattery Launcher",
                    onClick = { /* Could show about dialog */ }
                )
            }

            item {
                SettingChip(
                    label = "Redefinir Padrões",
                    secondaryLabel = "Restaurar configurações",
                    onClick = {
                        scope.launch {
                            preferences.clear()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingChip(
    label: String,
    secondaryLabel: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Chip(
        label = { Text(text = label) },
        secondaryLabel = secondaryLabel?.let { { Text(text = it) } },
        onClick = onClick,
        colors = ChipDefaults.primaryChipColors(),
        modifier = modifier.padding(vertical = 4.dp)
    )
}

