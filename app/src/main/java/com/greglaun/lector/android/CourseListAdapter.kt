package com.greglaun.lector.android

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.greglaun.lector.R
import com.greglaun.lector.data.course.CourseContext

class CourseListAdapter(val courseList: MutableList<CourseContext>,
                        private val onItemClicked: (CourseContext) -> Unit,
                        private val onItemLongClicked: (CourseContext) -> Unit) :
        RecyclerView.Adapter<CourseListAdapter.courseListViewHolder>() {

    class courseListViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CourseListAdapter.courseListViewHolder {
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.course_list_text_view, parent, false) as TextView
        return courseListViewHolder(textView)
    }

    override fun onBindViewHolder(holder: courseListViewHolder, position: Int) {
        holder.textView.text = courseList[position].courseName
        holder.textView.setOnClickListener {
            onItemClicked.invoke(courseList[position])
        }
        holder.textView.setOnLongClickListener {
            onItemLongClicked.invoke(courseList[position])
            true
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = courseList.size
}

