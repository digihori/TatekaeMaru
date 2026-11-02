package tk.horiuchi.tatekaemaru.platform

import platform.Foundation.NSUUID

internal actual fun newId(): String = NSUUID().UUIDString.lowercase()
