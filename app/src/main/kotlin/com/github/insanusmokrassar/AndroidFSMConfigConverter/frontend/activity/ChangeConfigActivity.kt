package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.activity

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.insanusmokrassar.FSMConfigConverter.compileFromConfig
import com.github.insanusmokrassar.FSMConfigConverter.getContent
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.Config
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.getConfigsDatabases
import io.reactivex.Observable
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
        setContentView(R.layout.activity_realtime)
        val databaseHelper = getConfigsDatabases()
        var lastJob: Job? = null
        val compiledTableTextView = findViewById(R.id.compiledTableTextView) as TextView
        val rulesEditText: EditText = (findViewById(R.id.inputRulesEditText) as EditText)

        val delayObservable = PublishSubject.create<Editable>()
        delayObservable.debounce (
                resources.getInteger(R.integer.awaitEditedTimeInMillis).toLong(),
                TimeUnit.MILLISECONDS
        ).subscribe({
            try {
                config.config = it.toString()
                databaseHelper.upsert(config)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        rulesEditText.addTextChangedListener(
                object: TextWatcher {
                    override fun afterTextChanged(editable: Editable) {
                        delayObservable.onNext(editable)
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                }
        )

        compiledTableTextView.setOnClickListener {
            if (compiledTableTextView.text.isNotEmpty()) {
                val shareIntent = Intent()
                shareIntent.action = ACTION_SEND
                shareIntent.putExtra(EXTRA_TEXT, compiledTableTextView.text)
                shareIntent.type = "text/plain"
                startActivity(shareIntent)
            }
        }
        findViewById(R.id.compileConfigBtn).setOnClickListener {
            lastJob ?. cancel()
            lastJob = startCompile(rulesEditText.text.toString(), compiledTableTextView)
        }
    }

    private fun startCompile(from: String, tableTextView: TextView): Job {
        return async {
            val content: String = try {
                Log.i(ChangeConfigActivity::class.java.simpleName, "Thread: " + Thread.currentThread().name)
                val preStates = compileFromConfig(from)
                getContent(
                        getString(R.string.realtimeGenerating),
                        preStates
                )
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
