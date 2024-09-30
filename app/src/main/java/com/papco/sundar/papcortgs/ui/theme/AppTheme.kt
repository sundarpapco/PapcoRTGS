package com.papco.sundar.papcortgs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun RTGSTheme(isDarkTheme:Boolean= isSystemInDarkTheme(),content: @Composable ()->Unit){
    MaterialTheme(
        colorScheme = lightColorTheme,
        content = content
    )
}