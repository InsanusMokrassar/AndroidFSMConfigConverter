package com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

val nativeTypesMap = mapOf(
        Pair(
                Int::class,
                "INTEGER"
        ),
        Pair(
                Long::class,
                "LONG"
        ),
        Pair(
                Float::class,
                "FLOAT"
        ),
        Pair(
                Double::class,
                "DOUBLE"
        ),
        Pair(
                String::class,
                "TEXT"
        ),
        Pair(
                Boolean::class,
                "BOOLEAN"
        )
)

internal fun KClass<*>.tableName(): String {
    return java.simpleName
}

fun Map<KProperty<*>, Any>.toContentValues(): ContentValues {
    val cv = ContentValues()
    keys.forEach {
        val prop = it
        val value = get(prop)!!
        when(value::class) {
            Boolean::class -> cv.put(prop.name, value as Boolean)
            Int::class -> cv.put(prop.name, value as Int)
            Long::class -> cv.put(prop.name, value as Long)
            Float::class -> cv.put(prop.name, value as Float)
            Double::class -> cv.put(prop.name, value as Double)
            Byte::class -> cv.put(prop.name, value as Byte)
            ByteArray::class -> cv.put(prop.name, value as ByteArray)
            String::class -> cv.put(prop.name, value as String)
            Short::class -> cv.put(prop.name, value as Short)
        }
    }
    return cv
}

fun Any.toContentValues(): ContentValues {
    return toValuesMap().toContentValues()
}

fun Any.getVariablesMap(): Map<String, KProperty<*>> {
    val futureMap = LinkedHashMap<String, KProperty<*>>()
    this::class.getVariables().forEach {
        futureMap.put(it.name, it)
    }
    return futureMap
}

fun Any.toValuesMap() : Map<KProperty<*>, Any> {
    val values = HashMap<KProperty<*>, Any>()

    getVariablesMap().values.filter {
        it.intsanceKClass() != Any::class && (!it.returnType.isMarkedNullable || it.call(this) != null)
    }.forEach {
        it.call(this)?.let { value ->
            values.put(
                    it,
                    value
            )
        }
    }
    return values
}

fun <M: Any> KClass<M>.fromValuesMap(values : Map<KProperty<*>, Any>): M {
    if (constructors.isEmpty()) {
        throw IllegalStateException("For some of reason, can't create correct realisation of model")
    } else {
        val constructorRequiredVariables = getRequiredInConstructor()
        val resultModelConstructor = constructors.first {
            if (it.parameters.size != constructorRequiredVariables.size + 1) {
                return@first false
            }
            it.parameters.indices
                    .filter { i -> i < 1 || it.parameters[i].type.classifier != constructorRequiredVariables[i - 1].returnType.classifier }
                    .forEach { return@first false }
            true
        }
        val paramsList = ArrayList<Any?>()
        constructorRequiredVariables.forEach {
            paramsList.add(
                    values[it]
            )
        }
        val result = resultModelConstructor.call(*paramsList.toTypedArray())
        values.keys.forEach {
            if (!constructorRequiredVariables.contains(it)) {
                (it as KMutableProperty).setter.call(result, values[it])
            }
        }
        return result
    }
}

fun <M: Any> Cursor.extractAllAndClose(modelClass: KClass<M>): M {
    val properties = modelClass.getVariablesMap()
    val values = HashMap<KProperty<*>, Any>()
    properties.values.forEach {
        values.put(
                it, extras[it.name]
        )
    }
    return modelClass.fromValuesMap(values)
}

fun <M: Any> Cursor.extractAll(modelClass: KClass<M>, close: Boolean = true): List<M> {
    val result = ArrayList<M>()
    if (moveToFirst()) {
        do {
            result.add(extractAllAndClose(modelClass))
        } while (moveToNext())
    }
    if (close) {
        close()
    }
    return result
}

fun <M : Any> SQLiteDatabase.createTableIfNotExist(modelClass: KClass<M>) {
    val fieldsBuilder = StringBuilder()
    val primaryFields = modelClass.getPrimaryFields()

    modelClass.getVariables().forEach {
        if (it.isReturnNative()) {
            fieldsBuilder.append("${it.name} ${nativeTypesMap[it.returnClass()]}")
            if (!it.isNullable()) {
                fieldsBuilder.append(" NOT NULL")
            }
            if (primaryFields.contains(it) && it.isAutoincrement()) {
                fieldsBuilder.append(" AUTOINCREMENT")
            }
        } else {
            TODO()
        }
        fieldsBuilder.append(", ")
    }
    if (primaryFields.isNotEmpty()) {
        fieldsBuilder.append("CONSTRAINT ${modelClass.tableName()}_PR_KEY PRIMARY KEY (")
        primaryFields.forEach {
            fieldsBuilder.append(it.name)
            if (!primaryFields.isLast(it)) {
                fieldsBuilder.append(", ")
            }
        }
        fieldsBuilder.append(")")
    }

    try {
        execSQL("CREATE TABLE IF NOT EXISTS ${modelClass.tableName()} ($fieldsBuilder);")
        Log.i("createTableIfNotExist", "Table ${modelClass.tableName()} was created")
    } catch (e: Exception) {
        Log.e("createTableIfNotExist", "init", e)
        throw IllegalArgumentException("Can't create table ${modelClass.tableName()}", e)
    }
}


