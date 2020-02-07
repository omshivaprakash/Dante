package at.shockbytes.dante.core

import android.content.Context
import android.content.Intent
import at.shockbytes.dante.core.book.BareBoneBook
import at.shockbytes.dante.core.book.BookEntity
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
fun BookEntity.toJson(): JsonObject {
    return JsonObject().apply {
        addProperty("position", position)
        addProperty("title", title)
        addProperty("subTitle", subTitle)
        addProperty("author", author)
        addProperty("pageCount", pageCount)
        addProperty("publishedDate", publishedDate)
        addProperty("isbn", isbn)
        addProperty("language", language)
        addProperty("currentPage", currentPage)
        addProperty("notes", notes)
        addProperty("thumbnailAddress", thumbnailAddress)
        addProperty("googleBooksLink", googleBooksLink)
        addProperty("ordinalState", state.ordinal)
        addProperty("rating", rating)
        addProperty("startDate", startDate)
        addProperty("endDate", endDate)
        addProperty("wishlistDate", wishlistDate)
        addProperty("summary", summary)
        add("labels", JsonArray().apply { labels.forEach { add(it) } })
    }
}

fun BookEntity.createSharingIntent(c: Context): Intent {
    val msg = c.getString(R.string.share_template, this.title, this.googleBooksLink)
    return Intent()
        .setAction(Intent.ACTION_SEND)
        .putExtra(Intent.EXTRA_TEXT, msg)
        .setType("text/plain")
}

fun BookEntity.bareBone(): BareBoneBook {
    return BareBoneBook(
        title = title,
        author = author,
        thumbnailAddress = thumbnailAddress
    )
}

/**
 * The only visible update is the page count, but check the dates as well,
 * because this prevents an error of not overriding newly set dates in the main list.
 * To be sure check basically everything what can change!
 */
fun BookEntity.isContentSame(other: BookEntity): Boolean {
    return (this.id == other.id) &&
        (this.currentPage == other.currentPage) &&
        (this.pageCount == other.pageCount) &&
        (this.subTitle == other.subTitle) &&
        (this.author == other.author) &&
        (this.thumbnailAddress == other.thumbnailAddress) &&
        (this.title == other.title)
}