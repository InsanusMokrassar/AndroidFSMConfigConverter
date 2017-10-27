package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView.ConfigViewHolder
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView.abstracts.RecyclerViewAdapter
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.Config
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.getConfigsDatabases
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class ConfigsListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configs_list)
    }

    private val optionsMenuCallbacks = mapOf (
            Pair(
                    R.id.createConfigMenuItem,
                    {
                        val intent = Intent(this, ChangeConfigActivity::class.java)
                        intent.putExtra(getString(R.string.configToEdit), Config())
                        startActivity(intent)
                        true
                    }
            )
    )

    override fun onResume() {
        super.onResume()
        val configsList = findViewById(R.id.configsListRecyclerView) as RecyclerView
        val adapter = RecyclerViewAdapter(
                {
                    parent: ViewGroup, _: Int ->
                    ConfigViewHolder(layoutInflater, parent)
                }
        )
        adapter.emptyView = findViewById(R.id.emptyConfigsTextView)
        configsList.adapter = adapter
        async {
            try {
                val configs = getConfigsDatabases().find().toTypedArray()
                launch(UI) {
                    adapter.addItems(
                            *configs
                    )
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater ?. inflate(R.menu.activity_configs_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return optionsMenuCallbacks[item.itemId] ?. invoke() ?: super.onOptionsItemSelected(item)
    }
}
