package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackAnimatable
import at.shockbytes.dante.ui.fragment.BookDetailFragment
import at.shockbytes.dante.flagging.FeatureFlagging
import javax.inject.Inject

class DetailActivity : TintableBackNavigableActivity() {

    @Inject
    lateinit var featureFlagging: FeatureFlagging

    private var detailFragment: BackAnimatable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra(ARG_ID, -1)
        val title = intent.getStringExtra(ARG_TITLE)

        if (id != -1L) {
            supportActionBar?.title = title
            detailFragment = pickDetailFragment(id)
        } else {
            supportFinishAfterTransition()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun backwardAnimation() {
        super.backwardAnimation()
        performBackwardAnimation()
    }

    override fun onBackStackPopped() {
        super.onBackStackPopped()
        performBackwardAnimation()
    }

    private fun performBackwardAnimation() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            detailFragment?.onBackwardAnimation()
        }
    }

    private fun pickDetailFragment(id: Long): BackAnimatable {

        val fragment = BookDetailFragment.newInstance(id)

        supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(android.R.id.content, fragment)
                .commit()

        return fragment
    }

    companion object {

        private const val ARG_ID = "arg_id"
        private const val ARG_TITLE = "arg_title"

        fun newIntent(context: Context, id: Long, title: String): Intent {
            return Intent(context, DetailActivity::class.java)
                    .putExtra(ARG_ID, id)
                    .putExtra(ARG_TITLE, title)
        }
    }
}
