package tk.horiuchi.tatekaemaru.platform

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

internal expect fun newId(): String

fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
