package com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment

import android.content.Context
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.Autoincrement
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.PrimaryKey
import java.io.Serializable

fun Context.getConfigsDatabases(): ConfigsDatabase {
    return ConfigsDatabase(this)
}

class ConfigsDatabase(context: Context): SimpleDatabase<Config>(
        Config::class,
        context,
        context.getString(R.string.commonDatabase),
        context.resources.getInteger(R.integer.configDatabaseCurrentVersion)
) {
    fun upsert(config: Config) {
        if (config.id == null) {
            insert(config)
            config.id = find("rules=\"${config.rules}\" AND title=\"${config.title}\"").first().id
        } else {
            update(config)
        }
    }

    fun remove(config: Config) {
        if (config.id != null) {
            remove("id=${config.id}")
        }
    }
}

data class Config internal constructor(
        var rules: String = "",
        var title: String = "",
        @PrimaryKey
        @Autoincrement
        internal var id: Int? = null
) : Serializable {
    constructor(another: Config) : this(another.rules, another.title, another.id)
}
