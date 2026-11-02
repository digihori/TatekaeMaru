package tk.horiuchi.tatekaemaru.platform

import java.util.UUID

internal actual fun newId(): String = UUID.randomUUID().toString()
