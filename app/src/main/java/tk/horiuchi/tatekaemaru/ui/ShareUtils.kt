package tk.horiuchi.tatekaemaru.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareUtils {
    private const val LINE_PACKAGE_NAME = "jp.naver.line.android"

    fun shareText(context: Context, text: String, chooserTitle: String = "共有先を選択") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    fun shareTextToLine(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage(LINE_PACKAGE_NAME)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "LINEが見つかりません。共有先を選択してください。", Toast.LENGTH_SHORT).show()
            shareText(context, text, "精算結果を共有")
        }
    }
}
