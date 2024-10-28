package com.papco.sundar.papcortgs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

private val lightColorTheme= lightColorScheme(
    primary = colorPrimary,
    onPrimary = colorOnPrimary,
    primaryContainer = colorPrimaryContainer,
    onPrimaryContainer = colorOnPrimaryContainer,
    inversePrimary = colorInversePrimary,
    secondary = colorSecondary,
    onSecondary = colorOnSecondary,
    secondaryContainer = colorSecondaryContainer,
    onSecondaryContainer = colorOnSecondaryContainer,
    tertiary = colorTertiary,
    onTertiary = colorOnTertiary,
    tertiaryContainer = colorTertiaryContainer,
    onTertiaryContainer = colorOnTertiaryContainer,
    surface = colorSurface,
    surfaceBright = colorSurfaceBright,
    surfaceDim = colorSurfaceDim,
    onSurface = colorOnSurface,
    onSurfaceVariant = colorOnSurfaceVariant,
    surfaceContainerLowest = colorSurfaceLowest,
    surfaceContainerLow = colorSurfaceLow,
    surfaceContainer = colorSurfaceContainer,
    surfaceContainerHigh = colorSurfaceContainerHigh,
    surfaceContainerHighest = colorSurfaceContainerHighest,
    outline = colorOutline,
    outlineVariant = colorOutlineVariant,
    inverseSurface = colorInverseSurface,
    inverseOnSurface = colorInverseOnSurface,
    error = colorError,
    onError = colorOnError,
    errorContainer = colorErrorContainer,
    onErrorContainer = colorOnErrorContainer,
    background = colorSurface
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTGSTheme(isDarkTheme:Boolean= isSystemInDarkTheme(),content: @Composable ()->Unit) {
    MaterialTheme(
        colorScheme = lightColorTheme
    ) {
        val rippleColour = MaterialTheme.colorScheme.primary
        val myRipple = remember {
            RippleConfiguration(rippleColour, RippleAlpha(0.1f, 0.1f, 0.08f, 0.4f))
        }

        CompositionLocalProvider(LocalRippleConfiguration provides myRipple) {
            content()
        }
    }
}

