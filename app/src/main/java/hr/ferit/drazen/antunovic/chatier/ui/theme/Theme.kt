package hr.ferit.drazen.antunovic.chatier.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Dark100,
    onSecondary = Color.White,
    background = Dark500,
    onBackground = Color.White,
    surface = Dark400,
    onSurface = Color.White,
    error = DarkRed,
    onError = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Dark500,
    onPrimary = Color.White,
    secondary = Light100,
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Dark500,
    surface = Color.White,
    onSurface = Dark500,
    error = LightRed,
    onError = Color.White,
)

@Composable
fun ChatierTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val systemUiController = rememberSystemUiController()
    val colors = if (darkTheme) {
        systemUiController.setSystemBarsColor(
            color = Dark500
        )
        DarkColorPalette
    } else {
        systemUiController.setSystemBarsColor(
            color = Color.White
        )
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
