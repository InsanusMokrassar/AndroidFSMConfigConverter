package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView.abstracts.AbstractViewHolder
import com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.DatabaseManagment.Config

class ConfigViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
): AbstractViewHolder<Config>(
        {
            layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        }
) {
    private var current: Config? = null
    override fun refreshItem(item: Config) {
        current = item
        itemView.findViewById<TextView>(android.R.id.text1).text = item.title
    }
}
