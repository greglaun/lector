package com.greglaun.lector.android

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.greglaun.lector.R
import com.greglaun.lector.data.cache.ArticleContext

class ReadingListAdapter(private val readingList: MutableList<ArticleContext>,
                         private val onItemClicked: (ArticleContext) -> Unit,
                         private val onItemLongClicked: (ArticleContext) -> Unit) :
        RecyclerView.Adapter<ReadingListAdapter.ReadingListViewHolder>() {

    class ReadingListViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ReadingListAdapter.ReadingListViewHolder {
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.reading_list_text_view, parent, false) as TextView
        return ReadingListViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ReadingListViewHolder, position: Int) {
        holder.textView.text = readingList[position].contextString
        holder.textView.setOnClickListener {
            onItemClicked.invoke(readingList[position])
        }
        holder.textView.setOnLongClickListener {
            onItemLongClicked.invoke(readingList[position])
            true
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = readingList.size
}

