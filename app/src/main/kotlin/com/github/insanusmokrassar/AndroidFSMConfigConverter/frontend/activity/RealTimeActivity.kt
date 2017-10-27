package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.activity

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.github.insanusmokrassar.FSMConfigConverter.compileFromConfig
import com.github.insanusmokrassar.FSMConfigConverter.getContent
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class RealTimeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime)
        var lastJob: Job? = null
        val compiledTableTextView = findViewById(R.id.compiledTableTextView) as TextView
        val rulesEditText: EditText = (findViewById(R.id.inputRulesEditText) as EditText)
//        rulesEditText.addTextChangedListener(
//                object: TextWatcher {
//                    override fun afterTextChanged(editable: Editable) {}
//
//                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                        lastJob ?. cancel()
//                        lastJob = null
//                        compiledTableTextView.text = getString(R.string.awaitInputComplete)
//                        compiledTableTextView.isClickable = false
//                    }
//
//                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                        editedTime = System.currentTimeMillis()
//                    }
//                }
//        )

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
                Log.i(RealTimeActivity::class.java.simpleName, "Thread: " + Thread.currentThread().name)
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
