package at.shockbytes.dante.backup

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.model.RestoreStrategy
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import io.reactivex.Completable
import io.reactivex.Single

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */
@Deprecated("Use BackupRepository instead")
interface BackupManager {

    var lastBackupTime: Long

    val backupList: Single<List<BackupMetadata>>

    @Throws(BackupServiceConnectionException::class)
    fun connect(activity: FragmentActivity): Completable

    fun close(books: List<BookEntity>? = null)

    fun removeBackupEntry(entry: BackupMetadata): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(books: List<BookEntity>): Completable

    fun restoreBackup(
        entry: BackupMetadata,
        bookDao: BookEntityDao,
        strategy: RestoreStrategy
    ): Completable
}
