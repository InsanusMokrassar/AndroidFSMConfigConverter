package com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment

import android.content.Context
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R

fun Context.getConfigsDatabases(): SimpleDatabase<Config> {
    return SimpleDatabase(
            Config::class,
            this,
            getString(R.string.commonDatabase),
            resources.getInteger(R.integer.configDatabaseCurrentVersion)
    )
}

data class Config internal constructor(
        val config: String,
        val name: String,
        internal var id: Long? = null
)
