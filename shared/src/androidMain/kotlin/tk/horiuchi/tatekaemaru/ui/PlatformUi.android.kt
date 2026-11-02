package tk.horiuchi.tatekaemaru.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon

@Composable
internal actual fun PlatformBackHandler(onBack: () -> Unit) = BackHandler(onBack = onBack)

@Composable
internal actual fun OverflowMenuIcon() {
    Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
}
