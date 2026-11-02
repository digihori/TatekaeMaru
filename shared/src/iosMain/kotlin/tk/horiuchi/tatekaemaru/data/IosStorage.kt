@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package tk.horiuchi.tatekaemaru.data

import platform.Foundation.*

class IosAppSettings(private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults) : AppSettings {
    override var initialSampleCreated: Boolean
        get() = defaults.boolForKey("initial_sample_created_v1")
        set(value) { defaults.setBool(value, "initial_sample_created_v1") }
    override var myName: String
        get() = defaults.stringForKey("my_name") ?: "自分"
        set(value) { defaults.setObject(value, "my_name") }
}

class IosEventDocumentStorage : EventDocumentStorage {
    private val path: String

    init {
        val directory = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
            .first() as String
        check(NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null)) {
            "保存先を作成できませんでした。"
        }
        path = "$directory/events-v1.json"
    }

    override fun read(): String? {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as String?
            ?: error("保存データを読み込めませんでした。")
    }

    override fun write(document: String) {
        check(NSString.create(string = document).writeToFile(path, true, NSUTF8StringEncoding, null)) {
            "保存できませんでした。端末の空き容量を確認してください。"
        }
    }
}
