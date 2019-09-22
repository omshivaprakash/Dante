package at.shockbytes.dante.ui.fragment

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.ui.adapter.BookAdapter
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.image.ImageLoadingCallback
import at.shockbytes.dante.util.AnimationUtils
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.addTo
import at.shockbytes.util.adapter.BaseAdapter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_download_book.*
import timber.log.Timber
import javax.inject.Inject

class DownloadBookFragment :
    BaseFragment(),
    ImageLoadingCallback,
    Palette.PaletteAsyncListener,
    BaseAdapter.OnItemClickListener<BookEntity> {

    interface OnBookDownloadedListener {

        fun onBookDownloaded(book: BookEntity)

        fun onCancelDownload()

        fun onErrorDownload(reason: String, isAttached: Boolean)

        fun onCloseOnError()

        fun colorSystemBars(
            actionBarColor: Int?,
            actionBarTextColor: Int?,
            statusBarColor: Int?,
            title: String?
        )
    }

    private val drawableResList: List<Pair<Int, TextView>> by lazy {
        listOf(Pair(R.drawable.ic_pick_upcoming, btnDownloadFragmentUpcoming),
                Pair(R.drawable.ic_pick_current, btnDownloadFragmentCurrent),
                Pair(R.drawable.ic_pick_done, btnDownloadFragmentDone))
    }

    private val animViews: List<View> by lazy {
        listOf(btnDownloadFragmentUpcoming,
                btnDownloadFragmentCurrent,
                btnDownloadFragmentDone,
                btnDownloadFragmentNotMyBook)
    }

    @Inject
    lateinit var bookDownloader: BookDownloader

    @Inject
    lateinit var bookDao: BookEntityDao

    @Inject
    lateinit var imageLoader: ImageLoader

    private var bookAdapter: BookAdapter? = null
    private var listener: OnBookDownloadedListener? = null
    private var selectedBook: BookEntity? = null
    private var isOtherSuggestionsShowing: Boolean = false
    private var query: String? = null

    override val layoutId = R.layout.fragment_download_book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = arguments?.getString(argBarcode)
        isOtherSuggestionsShowing = false
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun setupViews() {
        loadIcons()
        downloadBook()

        btnDownloadFragmentErrorClose.setOnClickListener {
            listener?.onCloseOnError()
        }
        btnDownloadFragmentNotMyBook.setOnClickListener {
            showOtherSuggestions()
        }
        btnDownloadFragmentUpcoming.setOnClickListener {
            finishBookDownload(BookState.READ_LATER)
        }
        btnDownloadFragmentCurrent.setOnClickListener {
            finishBookDownload(BookState.READING)
        }
        btnDownloadFragmentDone.setOnClickListener {
            finishBookDownload(BookState.READ)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnBookDownloadedListener
    }

    override fun onImageLoadingFailed(e: Exception?) {
        Timber.e(e)
    }

    override fun onImageResourceReady(resource: Drawable?) {
        (resource as? BitmapDrawable)?.bitmap?.let {
            Palette.from(it).generate(this)
        }
    }

    override fun bindViewModel() {
        // Not needed...
    }

    override fun unbindViewModel() {
        // Not needed...
    }

    override fun onGenerated(palette: Palette?) {

        val actionBarColor = palette?.lightMutedSwatch?.rgb
        val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette?.darkMutedSwatch?.rgb

        listener?.colorSystemBars(
            actionBarColor,
            actionBarTextColor,
            statusBarColor,
            selectedBook?.title
        )
    }

    override fun onItemClick(t: BookEntity, v: View) {
        val index = bookAdapter?.getLocation(t) ?: return
        bookAdapter?.deleteEntity(t)
        bookAdapter?.addEntity(index, selectedBook!!)

        selectedBook = t

        setTitle(selectedBook?.title)
        setIcon(selectedBook?.thumbnailAddress)
        setAuthor(selectedBook?.author)

        recyclerViewDownloadFragmentOtherSuggestions.scrollToPosition(0)
    }

    private fun setAuthor(author: String?) {
        txtDownloadFragmentAuthor.text = author
    }

    private fun showOtherSuggestions() {

        if (!isOtherSuggestionsShowing) {

            txtDownloadFragmentTitle.animate()
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(AnticipateInterpolator())
                .withEndAction {
                    recyclerViewDownloadFragmentOtherSuggestions.animate()
                        .alpha(1f)
                        .start()
                    txtDownloadFragmentOtherSuggestions.animate()
                        .alpha(1f)
                        .start()
                }
                .start()

            txtDownloadFragmentAuthor.animate()
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(AnticipateInterpolator())
                .start()

            imgViewDownloadFragmentCover.animate()
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(AnticipateInterpolator())
                .start()

            btnDownloadFragmentNotMyBook.setText(R.string.download_suggestions_none)

            isOtherSuggestionsShowing = true
        } else {
            listener?.onCancelDownload()
        }
    }

    private fun finishBookDownload(bookState: BookState) {
        selectedBook?.let { book ->
            // Set the state and store it in database
            book.updateState(bookState)
            bookDao.create(book)
            listener?.onBookDownloaded(book)
        }
    }

    private fun downloadBook() {
        query?.let { q ->
            bookDownloader.downloadBook(q).subscribe({ suggestion ->
                animateBookViews()
                if (suggestion != null && suggestion.hasSuggestions) {
                    selectedBook = suggestion.mainSuggestion

                    setIcon(selectedBook?.thumbnailAddress)
                    setTitle(selectedBook?.title)
                    setAuthor(selectedBook?.author)

                    setupOtherSuggestionsRecyclerView(suggestion.otherSuggestions)

                    setActionBarTitle(selectedBook?.title)
                } else {
                    listener?.onErrorDownload("no suggestions", isAdded)
                    showErrorLayout(getString(R.string.download_book_json_error))
                }
            }) { throwable: Throwable? ->
                throwable?.printStackTrace()
                showErrorLayout(throwable)
                listener?.onErrorDownload(throwable?.localizedMessage
                        ?: "Error message not available", isAdded)
            }
        }
    }

    private fun setActionBarTitle(title: String?) {
        activity?.actionBar?.title = title
    }

    private fun loadIcons() {
        Single.fromCallable {
            drawableResList.mapNotNull { (drawableRes, view) ->
                context?.let { ctx ->
                    val drawable = DanteUtils.vector2Drawable(ctx, drawableRes)
                    Pair(drawable, view)
                }
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { list ->
            list.forEach { (drawable, view) ->
                view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            }
        }.addTo(compositeDisposable)
    }

    private fun animateBookViews() {
        // Hide progressbar smoothly
        progressBarDownloadFragment.animate()
                .alpha(0f)
                .scaleY(0.5f)
                .scaleX(0.5f)
                .setDuration(500)
                .start()
        AnimationUtils.detailEnterAnimation(animViews, 250, interpolator = OvershootInterpolator(4f))
    }

    private fun setTitle(title: String?) {
        txtDownloadFragmentTitle.text = title
    }

    private fun setIcon(address: String?) {
        if (!address.isNullOrEmpty()) {
            context?.let { ctx ->
                imageLoader.loadImage(
                    ctx,
                    address,
                    imgViewDownloadFragmentCover,
                    callback = this,
                    callbackHandleValues = Pair(first = false, second = true)
                )
            }
        } else {
            imgViewDownloadFragmentCover.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setupOtherSuggestionsRecyclerView(books: List<BookEntity>) {

        bookAdapter = BookAdapter(
                requireContext(),
                BookState.READ_LATER,
                imageLoader
        ).apply {
            this.onItemClickListener = this@DownloadBookFragment

            this.data = books
                    .map { book ->
                        book.apply {
                            state = BookState.READ_LATER
                        }
                    }
                    .toMutableList()
        }
        recyclerViewDownloadFragmentOtherSuggestions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = bookAdapter
        }
    }

    private fun showErrorLayout(error: Throwable?) {
        Timber.e(error)

        if (isAdded) {
            val cause = getString(R.string.download_code_error)
            showErrorLayout(cause)
        }
    }

    private fun showErrorLayout(cause: String) {
        if (isAdded) {
            layoutDownloadFragmentMain.visibility = View.GONE
            layoutDownloadFragmentError.visibility = View.VISIBLE
            layoutDownloadFragmentError.animate().alpha(1f).start()
            txtDownloadFragmentErrorCause.text = cause
        } else {
            showToast(cause, true)
            // Log this message, because this should not happen
            Timber.e(IllegalArgumentException("Cannot show error layout, because DownloadBookFragment is not attached to Activity"))
        }
    }

    companion object {

        private const val argBarcode = "arg_barcode"

        fun newInstance(barcode: String?): DownloadBookFragment {
            val fragment = DownloadBookFragment()
            val args = Bundle()
            args.putString(argBarcode, barcode)
            fragment.arguments = args
            return fragment
        }
    }
}
