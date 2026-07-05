package tk.horiuchi.tatekaemaru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tk.horiuchi.tatekaemaru.ui.TatekaeMaruApp
import tk.horiuchi.tatekaemaru.ui.theme.TatekaeMaruTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TatekaeMaruTheme {
                TatekaeMaruApp()
            }
        }
    }
}
