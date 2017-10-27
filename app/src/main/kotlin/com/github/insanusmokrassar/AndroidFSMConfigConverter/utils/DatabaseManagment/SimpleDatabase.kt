package com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.reflect.KClass

open class SimpleDatabase<M: Any> (
        private val modelClass: KClass<M>,
        context: Context,
        databaseName: String,
        version: Int):
        SQLiteOpenHelper(
        context,
        databaseName,
        null,
        version
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.createTableIfNotExist(modelClass::class)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //
        // This will throw exception if you upgrade version of database but not
        // override onUpgrade
    }

    fun insert(value: M): Boolean {
        return writableDatabase.insert(
                modelClass.tableName(),
                null,
                value.toContentValues()
        ) > 0
    }

    fun find(where: String, orderBy: String, limit: String): List<M> {
        return readableDatabase.query(
                modelClass.tableName(),
                null,
                where,
                null,
                null,
                null,
                orderBy,
                limit
        ).extractAll(modelClass, true)
    }

    fun update(value: M, where: String): Boolean {
        return writableDatabase.update(
                modelClass.tableName(),
                value.toContentValues(),
                where,
                null
        ) > 0
    }

    fun remove(where: String) {
        writableDatabase.delete(
                modelClass.tableName(),
                where,
                null
        )
    }
}
