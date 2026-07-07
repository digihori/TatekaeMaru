package tk.horiuchi.tatekaemaru.ui

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    private val yenFormatter = NumberFormat.getNumberInstance(Locale.JAPAN)

    fun yen(amount: Int): String = "${yenFormatter.format(amount)}円"
}
