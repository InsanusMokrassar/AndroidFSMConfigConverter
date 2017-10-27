package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R

class ChooseSourceActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_source)
        findViewById(R.id.realtimeBtn).setOnClickListener {
            val intent = Intent(this, ChangeConfigActivity::class.java)
            startActivity(intent)
        }
        findViewById(R.id.fileBtn).setOnClickListener {
            Toast.makeText(this, "It will be implemented as soon as possible", LENGTH_SHORT).show()
        }
    }
}
