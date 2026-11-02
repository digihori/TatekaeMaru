@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package tk.horiuchi.tatekaemaru.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*
import tk.horiuchi.tatekaemaru.data.*

fun MainViewController(): UIViewController {
    lateinit var controller: UIViewController
    controller = ComposeUIViewController {
        val dependencies = remember {
            runCatching { JsonEventRepository(IosEventDocumentStorage()) to IosAppSettings() }
        }
        val value = dependencies.getOrNull()
        if (value == null) {
            MaterialTheme { Text("保存先を開けませんでした。アプリを再起動してください。") }
        } else {
            SharedApp(
                repository = value.first,
                settings = value.second,
                versionName = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "不明",
                onShare = { text ->
                    val sheet = UIActivityViewController(listOf(text), null)
                    sheet.popoverPresentationController?.apply {
                        sourceView = controller.view
                        sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)
                        permittedArrowDirections = 0uL
                    }
                    controller.presentViewController(sheet, true, null)
                }
            )
        }
    }
    return controller
}
