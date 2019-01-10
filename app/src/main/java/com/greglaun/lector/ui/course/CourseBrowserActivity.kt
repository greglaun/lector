package com.greglaun.lector.ui.course

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class CourseBrowserActivity : AppCompatActivity(), CourseBrowserContract.View {
    private lateinit var courseBrowserRecyclerView: RecyclerView
    private lateinit var courseBrowserViewAdapter: RecyclerView.Adapter<*>
    private lateinit var courseBrowserViewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)


        courseBrowserViewAdapter = courseBrowserAdapter(courseBrowserPresenter.courseBrowser, { it: CourseContext ->
            courseBrowserPresenter.courseDetailsRequested(it)
        }, { it: CourseContext ->
            courseBrowserPresenter.deleteRequested(it)
        }
        )

        courseBrowserRecyclerView = findViewById<RecyclerView>(R.id.rv_course_browser).apply {
            setHasFixedSize(true)
            layoutManager = courseBrowserViewManager
            adapter = courseBrowserViewAdapter
        }

    }
}