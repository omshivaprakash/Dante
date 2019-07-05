package at.shockbytes.dante.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.DownloadBookFragment
import at.shockbytes.dante.ui.fragment.QueryCaptureFragment

/**
 * Author:  Martin Macheiner
 * Date:    01.01.2018
 */
class BookRetrievalActivity : TintableBackNavigableActivity(),
    QueryCaptureFragment.QueryCaptureCallback,
    DownloadBookFragment.OnBookDownloadedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_query_capture)
        // Set this, otherwise this will trigger a Kotlin Exception
        setResult(Activity.RESULT_CANCELED, Intent())

        val typeOrdinal = intent.getIntExtra(ARG_EXTRA_RETRIEVAL_ORDINAL, RetrievalType.CAMERA.ordinal)
        when (RetrievalType.values()[typeOrdinal]) {

            RetrievalType.CAMERA -> showQueryFragment()

            RetrievalType.TITLE -> {
                val query = intent.getStringExtra(ARG_EXTRA_RETRIEVAL_TITLE)
                onQueryAvailable(query)
            }
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCameraPermissionDenied() {
        supportFinishAfterTransition()
    }

    override fun onQueryAvailable(query: String?) {
        showDownloadFragment(query)
        tintHomeAsUpIndicator(R.drawable.ic_cancel)
    }

    override fun onScannerNotOperational() {
        showToast(R.string.scanner_not_operational)
    }

    override fun onBookDownloaded(book: BookEntity) {
        finishBookDownload()
    }

    override fun onCancelDownload() {
        finishBookDownload()
    }

    override fun onErrorDownload(reason: String, isAttached: Boolean) {

        if (!isAttached) {
            showToast(R.string.download_attachment_error)
            supportFinishAfterTransition()
        }
    }

    override fun onCloseOnError() {
        finishBookDownload()
    }

    override fun colorSystemBars(
        actionBarColor: Int?,
        actionBarTextColor: Int?,
        statusBarColor: Int?,
        title: String?
    ) {
        tintSystemBarsWithText(actionBarColor, actionBarTextColor, statusBarColor, title, true)
    }

    private fun finishBookDownload() {
        supportFinishAfterTransition()
    }

    private fun showQueryFragment() {
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, QueryCaptureFragment.newInstance())
                .commit()
    }

    private fun showDownloadFragment(query: String?) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(android.R.id.content, DownloadBookFragment.newInstance(query))
                .commit()
    }

    companion object {

        private const val ARG_EXTRA_RETRIEVAL_ORDINAL = "arg_retrieval_ordinal"
        private const val ARG_EXTRA_RETRIEVAL_TITLE = "arg_retrieval_title"

        fun newIntent(context: Context, retrievalType: RetrievalType, bookTitle: String?): Intent {
            return Intent(context, BookRetrievalActivity::class.java)
                    .putExtra(ARG_EXTRA_RETRIEVAL_ORDINAL, retrievalType.ordinal)
                    .putExtra(ARG_EXTRA_RETRIEVAL_TITLE, bookTitle)
        }
    }

    enum class RetrievalType {
        CAMERA, TITLE
    }
}