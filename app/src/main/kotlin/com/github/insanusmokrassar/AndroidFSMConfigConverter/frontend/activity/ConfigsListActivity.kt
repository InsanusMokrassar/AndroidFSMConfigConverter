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
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.extensions.checkPermissions
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.extensions.getRealPathFromURI
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.Config
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.getConfigsDatabases
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File

private val openFromFileCode = 43

class ConfigsListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configs_list)
        checkPermissions()
    }

    private val optionsMenuCallbacks = mapOf (
            Pair(
                    R.id.createConfigMenuItem,
                    {
                        openChangeConfigActivity()
                        true
                    }
            ),
            Pair(
                    R.id.openFromFileMenuItem,
                    {
                        val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                        chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
                        chooseFile.type = "*/*"
                        val intent = Intent.createChooser(
                                chooseFile,
                                getString(R.string.chooseFileWithScheme)
                        )
                        startActivityForResult(intent, openFromFileCode)
                        true
                    }
            )
    )

    private val onActivityResultsMap = mapOf<Int, (Intent?) -> Unit>(
            Pair(
                    openFromFileCode,
                    {
                        data ->
                        data ?. let {
                            val uri = it.data
                            val file = File(getRealPathFromURI(uri))
                            val config = Config(
                                    file.readText(),
                                    file.nameWithoutExtension
                            )
                            openChangeConfigActivity(config)
                        }
                    }
            )
    )

    override fun onResume() {
        super.onResume()
        val configsList = findViewById<RecyclerView>(R.id.configsListRecyclerView)
        val adapter = RecyclerViewAdapter(
                {
                    parent: ViewGroup, _: Int, adapter: RecyclerViewAdapter<Config> ->
                    ConfigViewHolder(adapter, layoutInflater, parent)
                }
        )
        adapter.emptyView = findViewById(R.id.emptyConfigsTextView)
        configsList.adapter = adapter
        async {
            try {
                val configs = getConfigsDatabases().find().toTypedArray()
                launch(UI) {
                    adapter.addItems(*configs)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        onActivityResultsMap[requestCode] ?. invoke(data)
    }

    private fun openChangeConfigActivity(config: Config = Config()) {
        val intent = Intent(this, ChangeConfigActivity::class.java)
        intent.putExtra(getString(R.string.configToEdit), config)
        startActivity(intent)
    }
}
