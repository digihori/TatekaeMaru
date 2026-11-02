package tk.horiuchi.tatekaemaru.data

import android.content.Context

class AndroidAppSettings(context: Context) : AppSettings {
    private val preferences = context.applicationContext.getSharedPreferences(
        "tatekae_maru_settings", Context.MODE_PRIVATE
    )

    override var initialSampleCreated: Boolean
        get() = preferences.getBoolean("initial_sample_created_v1", false)
        set(value) { preferences.edit().putBoolean("initial_sample_created_v1", value).apply() }

    override var myName: String
        get() = preferences.getString("my_name", "自分") ?: "自分"
        set(value) { preferences.edit().putString("my_name", value).apply() }
}
