package com.greglaun.lector.ui.course

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.greglaun.lector.R
import com.greglaun.lector.data.course.CourseContext



class courseBrowserAdapter(val courseBrowser: MutableList<CourseContext>,
                           private val onItemClicked: (CourseContext) -> Unit,
                           private val onItemLongClicked: (CourseContext) -> Unit) :
        RecyclerView.Adapter<courseBrowserAdapter.courseBrowserViewHolder>() {

    class courseBrowserViewHolder(val textView: TextView, val imageView: ImageView)
        : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): courseBrowserAdapter.courseBrowserViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.course_browse_adapter_layout, parent, false)
        return courseBrowserViewHolder(view.findViewById(R.id.textView) as TextView,
                view.findViewById(R.id.imgView) as ImageView)
    }

    override fun onBindViewHolder(holder: courseBrowserViewHolder, position: Int) {
        holder.textView.text = courseBrowser[position].courseName
        holder.textView.setOnClickListener {
            onItemClicked.invoke(courseBrowser[position])
        }
        holder.textView.setOnLongClickListener {
            onItemLongClicked.invoke(courseBrowser[position])
            true
        }
        // Note: For now we are not loading image 01/08/2019
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = courseBrowser.size
}

