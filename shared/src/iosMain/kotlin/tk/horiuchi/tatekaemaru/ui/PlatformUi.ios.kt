package tk.horiuchi.tatekaemaru.ui

import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon

@Composable
internal actual fun PlatformBackHandler(onBack: () -> Unit) { }

@Composable
internal actual fun OverflowMenuIcon() {
    Icon(Icons.Default.MoreHoriz, contentDescription = "メニュー")
}
