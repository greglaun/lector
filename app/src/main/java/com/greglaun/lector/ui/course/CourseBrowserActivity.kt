package com.greglaun.lector.ui.course

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.greglaun.lector.BuildConfig
import com.greglaun.lector.R
import com.greglaun.lector.android.room.LectorDatabase
import com.greglaun.lector.android.room.RoomCourseSource
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.CourseDownloaderImpl
import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.data.course.ThinCourseDetails
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CourseBrowserActivity : AppCompatActivity(), CourseBrowserContract.View {
    private lateinit var courseBrowserRecyclerView: RecyclerView
    private lateinit var courseBrowserViewAdapter: RecyclerView.Adapter<*>
    private lateinit var courseBrowserViewManager: RecyclerView.LayoutManager
    private lateinit var courseBrowserPresenter: CourseBrowserPresenter
    private lateinit var courseDetailLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_browse)
        courseBrowserPresenter = CourseBrowserPresenter(this,
                CourseDownloaderImpl(BuildConfig.BASE_URL, cacheDir),
                RoomCourseSource(LectorDatabase.getInstance(applicationContext)!!))

        courseBrowserViewManager = GridLayoutManager(this, 2)
        courseBrowserViewAdapter = CourseBrowserAdapter(courseBrowserPresenter.courseMetadatalist)
        {
            GlobalScope.launch {
                courseBrowserPresenter.onCourseDetailSelected(it)
            }
        }

        courseBrowserRecyclerView = findViewById<RecyclerView>(R.id.rv_course_browse).apply {
            setHasFixedSize(true)
            layoutManager = courseBrowserViewManager
            adapter = courseBrowserViewAdapter
        }
        courseDetailLayout = findViewById(R.id.details_layout)

        GlobalScope.launch {
            courseBrowserPresenter.beginCourseDownload()
        }
    }

    override fun onBackPressed() {
        if (courseBrowserRecyclerView.visibility != VISIBLE) {
            unHideCourseBrowseView()
        } else {
            NavUtils.navigateUpFromSameTask(this)
        }
    }

    private fun unHideCourseDetailsView() {
        courseBrowserRecyclerView.visibility = GONE
        courseDetailLayout.visibility = VISIBLE
    }

    private fun unHideCourseBrowseView() {
        courseDetailLayout.visibility = GONE
        courseBrowserRecyclerView.visibility = VISIBLE
    }

    override fun onCourseListChanged() {
        runOnUiThread {
            courseBrowserViewAdapter.notifyDataSetChanged()
        }
    }

    override fun showCourses(courses: List<CourseMetadata>) {
        runOnUiThread {
            unHideCourseBrowseView()
        }
    }

    override fun showCourseDetails(courseDetails: ThinCourseDetails) {
        val detailName = findViewById<TextView>(R.id.details_name)
        detailName.text = courseDetails.name
        val listView = findViewById<ListView>(R.id.article_list)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                courseDetails.articleNames.map {
                    urlToContext(it)
                })
        listView.adapter = adapter
        runOnUiThread {
            unHideCourseDetailsView()
        }
    }

    fun onSaveDetailsPressed(@Suppress("UNUSED_PARAMETER") view: View) {
        courseBrowserPresenter.onSaveDetailsPressed()
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

    override fun confirmMessage(message: String, yesButton: String, noButton: String, onConfirmed:
    (Boolean) -> Unit) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton(android.R.string.no) { _: DialogInterface, _: Int ->
                    onConfirmed(false)
                }
                .setPositiveButton(android.R.string.yes) { _: DialogInterface, _: Int ->
                    onConfirmed(true)
                }.create().show()
    }

    override fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun confirmMessage(resourceId: Int, yesButton: Int, noButton: String, onConfirmed: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}