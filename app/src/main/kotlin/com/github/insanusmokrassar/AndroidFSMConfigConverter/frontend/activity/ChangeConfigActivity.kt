package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.activity

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.Config
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.getConfigsDatabases
import com.github.insanusmokrassar.FSMConfigConverter.FSMRulesDescriptor
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class ChangeConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config: Config = intent.extras[getString(R.string.configToEdit)] as? Config ?: Config()
        setContentView(R.layout.activity_change_config)
        val databaseHelper = getConfigsDatabases()
        var lastJob: Job? = null
        val compiledTableTextView = findViewById<TextView>(R.id.compiledTableTextView)
        val configEditText = findViewById<TextView>(R.id.configRulesEditText)
        val titleEditText = findViewById<TextView>(R.id.configTitleEditText)

        configEditText.setText(config.rules)
        titleEditText.setText(config.title)

        val delayObservable = PublishSubject.create<Editable>()
        delayObservable.debounce (
                resources.getInteger(R.integer.awaitEditedTimeInMillis).toLong(),
                TimeUnit.MILLISECONDS
        ).subscribe({
            try {
                config.rules = configEditText.text.toString()
                config.title = if (titleEditText.text.isEmpty()) {
                    getString(R.string.tempTitle)
                } else {
                    titleEditText.text.toString()
                }
                databaseHelper.upsert(config)
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
        val changesWatcher = object: TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                delayObservable.onNext(editable)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        configEditText.addTextChangedListener(changesWatcher)
        titleEditText.addTextChangedListener(changesWatcher)

        compiledTableTextView.setOnClickListener {
            if (compiledTableTextView.text.isNotEmpty()) {
                val shareIntent = Intent()
                shareIntent.action = ACTION_SEND
                shareIntent.putExtra(EXTRA_TEXT, compiledTableTextView.text)
                shareIntent.type = "text/plain"
                startActivity(shareIntent)
            }
        }
        findViewById<View>(R.id.compileConfigBtn).setOnClickListener {
            lastJob ?. cancel()
            lastJob = startCompile(configEditText.text.toString(), compiledTableTextView)
        }
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
}
