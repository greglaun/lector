package com.greglaun.lector.ui.course

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.greglaun.lector.BuildConfig
import com.greglaun.lector.R
import com.greglaun.lector.android.room.LectorDatabase
import com.greglaun.lector.android.room.RoomCourseSource
import com.greglaun.lector.data.course.CourseDetails
import com.greglaun.lector.data.course.CourseDownloaderImpl
import com.greglaun.lector.data.course.CourseMetadata

class CourseBrowserActivity : AppCompatActivity(), CourseBrowserContract.View {
    private lateinit var courseBrowserRecyclerView: RecyclerView
    private lateinit var courseBrowserViewAdapter: RecyclerView.Adapter<*>
    private lateinit var courseBrowserViewManager: RecyclerView.LayoutManager
    private lateinit var courseBrowserPresenter: CourseBrowserPresenter

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_course_browse)
        courseBrowserPresenter = CourseBrowserPresenter(this,
                CourseDownloaderImpl(BuildConfig.BASE_URL, cacheDir),
                RoomCourseSource(LectorDatabase.getInstance(applicationContext)!!))
        courseBrowserViewAdapter = courseBrowserAdapter(courseBrowserPresenter.courseMetadatalist)
        { it: CourseMetadata ->
            courseBrowserPresenter.onCourseDetailSelected(it)
        }

        courseBrowserRecyclerView = findViewById<RecyclerView>(R.id.rv_course_browse).apply {
            setHasFixedSize(true)
            layoutManager = courseBrowserViewManager
            adapter = courseBrowserViewAdapter
        }
    }

    override fun showCourses(courses: List<CourseMetadata>) {
        runOnUiThread {
            unHideCourseBrowseView()
        }
    }

    override fun showCourseDetails(courseDetails: CourseDetails) {
        runOnUiThread {
            unHideCourseDetailsView()
        }
    }

    override fun onError(resourceId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(resourceId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun confirmMessage(message: String, yesButton: String, noButton: String, onConfirmed: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun confirmMessage(resourceId: Int, yesButton: Int, noButton: String, onConfirmed: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}