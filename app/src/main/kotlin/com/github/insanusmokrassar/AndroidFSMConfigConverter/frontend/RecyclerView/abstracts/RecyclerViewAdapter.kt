package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.RecyclerView.abstracts

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class RecyclerViewAdapter<T>(
        private val viewHolderFactory: (
                parent: ViewGroup,
                viewType: Int
        ) -> AbstractViewHolder<T>
): RecyclerView.Adapter<AbstractViewHolder<T>>() {
    private val data = ArrayList<T>()
    var emptyView: View? = null

    init {
        registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()
                        checkEmpty()
                    }
                }
        )
        checkEmpty()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: AbstractViewHolder<T>, position: Int) {
        holder.refreshItem(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder<T> =
            viewHolderFactory(parent!!, viewType)

    fun addItems(vararg items: T) {
        data.addAll(items)
        notifyItemRangeChanged(data.size - items.size, data.size)
    }

    fun removeItem(i: Int) {
        data.removeAt(i)
        notifyItemRangeChanged(i, data.size)
    }

    private fun checkEmpty() {
        emptyView ?. let {
            if (data.isEmpty()) {
                launch(UI) {
                    it.visibility = View.VISIBLE
                }
            } else {
                launch(UI) {
                    it.visibility = View.GONE
                }
            }
        }
    }
}
