package com.greglaun.lector.ui.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.greglaun.lector.R
import com.greglaun.lector.data.course.CourseMetadata


class CourseBrowserAdapter(private val courseBrowser: MutableList<CourseMetadata>,
                           private val onItemClicked: (CourseMetadata) -> Unit) :
        RecyclerView.Adapter<CourseBrowserAdapter.CourseBrowserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CourseBrowserAdapter.CourseBrowserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_course_browse, parent, false)
        return CourseBrowserViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseBrowserViewHolder, position: Int) {
        holder.view.setOnClickListener {
            onItemClicked.invoke(courseBrowser[position])
        }
        holder.textView.text = courseBrowser[position].name
        // Note: For now we are not loading image 01/08/2019
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = courseBrowser.size

    inner class CourseBrowserViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView) as TextView
    }

}

