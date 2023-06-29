package hr.ferit.drazen.antunovic.chatier

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import hr.ferit.drazen.antunovic.chatier.ui.Main
import hr.ferit.drazen.antunovic.chatier.ui.theme.ChatierTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatierTheme {
                Main()
            }
        }
    }
}
