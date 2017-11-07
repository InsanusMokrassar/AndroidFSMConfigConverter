package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView.abstracts.AbstractViewHolder
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView.abstracts.RecyclerViewAdapter
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.activity.ChangeConfigActivity
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.Config
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.getConfigsDatabases

class ConfigViewHolder(
        private val adapter: RecyclerViewAdapter<Config>,
        layoutInflater: LayoutInflater,
        parent: ViewGroup
): AbstractViewHolder<Config>(
        {
            layoutInflater.inflate(
                    R.layout.item_config_preview,
                    parent,
                    false
            )
        }
) {
    private var current: Config? = null
    override fun refreshItem(item: Config) {
        if (current == null) {
            itemView.findViewById<View>(android.R.id.text1).setOnClickListener {
                current ?. let {
                    val context = itemView.context
                    val intent = Intent(context, ChangeConfigActivity::class.java)
                    intent.putExtra(context.getString(R.string.configToEdit), it)
                    context.startActivity(intent)
                }
            }
            itemView.findViewById<View>(R.id.deleteConfigImageView).setOnClickListener {
                current ?. let {
                    itemView.context.getConfigsDatabases().remove(it)
                    adapter.removeItem(it)
                }
            }
        }
        current = item
        itemView.findViewById<TextView>(android.R.id.text1).text = item.title
    }
}
