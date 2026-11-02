package tk.horiuchi.tatekaemaru.ui

object FormatUtils {
    fun yen(amount: Int): String {
        val value = amount.toLong()
        val digits = (if (value < 0) -value else value).toString()
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return (if (value < 0) "-" else "") + grouped + "円"
    }
}
