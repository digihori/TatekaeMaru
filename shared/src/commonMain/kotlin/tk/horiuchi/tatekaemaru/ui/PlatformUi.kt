package tk.horiuchi.tatekaemaru.ui

import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal expect fun OverflowMenuIcon()

@Composable
internal expect fun PlatformBackHandler(onBack: () -> Unit)

internal fun defaultEventName(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return now.date.toString().replace('-', '/') + " " +
        now.hour.toString().padStart(2, '0') + ":" + now.minute.toString().padStart(2, '0')
}
