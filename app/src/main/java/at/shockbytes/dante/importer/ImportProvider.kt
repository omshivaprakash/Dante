package at.shockbytes.dante.importer

import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.Single
import java.io.File

interface ImportProvider {

    val importer: Importer

    fun importFromFile(file: File): Single<List<BookEntity>>
}