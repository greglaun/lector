package com.greglaun.lector.android


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.greglaun.lector.R
import com.greglaun.lector.data.course.CourseContext

class CourseListAdapter(private val courseList: MutableList<CourseContext>,
                        private val onItemClicked: (CourseContext) -> Unit,
                        private val onItemLongClicked: (CourseContext) -> Unit) :
        RecyclerView.Adapter<CourseListAdapter.CourseListViewHolder>() {

    class CourseListViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CourseListAdapter.CourseListViewHolder {
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.course_list_text_view, parent, false) as TextView
        return CourseListViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CourseListViewHolder, position: Int) {
        holder.textView.text = courseList[position].courseName
        holder.textView.setOnClickListener {
            onItemClicked.invoke(courseList[position])
        }
        holder.textView.setOnLongClickListener {
            onItemLongClicked.invoke(courseList[position])
            true
        }
    }

    override fun getItemCount() = courseList.size
}

