package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.Config
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.ConfigsDatabase
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.getConfigsDatabases
import com.github.insanusmokrassar.FSMConfigConverter.FSMRulesDescriptor
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class ChangeConfigActivity : AppCompatActivity() {
    var configEditText: TextView? = null
    var titleEditText: TextView? = null
    var config: Config = Config()

    var databaseHelper: ConfigsDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = intent.extras[getString(R.string.configToEdit)] as? Config
                ?: Config()
        setContentView(R.layout.activity_change_config)
        databaseHelper = getConfigsDatabases()
        var lastJob: Job? = null
        val compiledTableTextView = findViewById<TextView>(R.id.compiledTableTextView)
        configEditText = findViewById(R.id.configRulesEditText)
        titleEditText = findViewById(R.id.configTitleEditText)

        configEditText ?. text = config.rules
        titleEditText ?. text = config.title

        val editChangeable = PublishSubject.create<Editable>()
        val editObservable = editChangeable.debounce (
                resources.getInteger(R.integer.awaitEditedTimeInMillis).toLong(),
                TimeUnit.MILLISECONDS
        )
        editObservable.subscribe({
            try {
                config.rules = configEditText ?. text.toString()
                config.title = if (titleEditText ?. text ?. isEmpty() == true) {
                    getString(R.string.tempTitle)
                } else {
                    titleEditText ?. text.toString()
                }
                databaseHelper ?. upsert(config)
                launch (UI) {
                    Toast.makeText(
                            this@ChangeConfigActivity,
                            getString(R.string.updated),
                            LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        editObservable.subscribe({
            lastJob ?. cancel()
            lastJob = startCompile(configEditText ?. text.toString(), compiledTableTextView)
        })
        val changesWatcher = object: TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                editChangeable.onNext(editable)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        configEditText ?. addTextChangedListener(changesWatcher)
        titleEditText ?. addTextChangedListener(changesWatcher)
    }

    private fun startCompile(from: String, tableTextView: TextView): Job {
        return async {
            val content: String = try {
                Log.i(ChangeConfigActivity::class.java.simpleName, "Thread: " + Thread.currentThread().name)
                val descriptor = FSMRulesDescriptor(from)
                descriptor.markdownContent
            } catch (e: Exception) {
                getString(R.string.wrongExpression)
            }
            launch(UI) {
                tableTextView.text = content
                tableTextView.isClickable = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        config.rules = configEditText ?. text.toString()
        config.title = if (titleEditText ?. text ?. isEmpty() == true) {
            getString(R.string.tempTitle)
        } else {
            titleEditText ?. text.toString()
        }
        databaseHelper ?. upsert(config) ?: getConfigsDatabases().upsert(config)
    }
}
