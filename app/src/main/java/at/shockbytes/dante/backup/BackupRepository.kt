package at.shockbytes.dante.backup

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.data.BookRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    06.05.2019
 */
interface BackupRepository {

    val backupProvider: List<BackupProvider>

    fun setLastBackupTime(timeInMillis: Long)

    fun observeLastBackupTime(): Observable<Long>

    fun getBackups(): Single<List<BackupMetadataState>>

    fun initialize(activity: FragmentActivity, forceReload: Boolean): Completable

    fun close(): Completable

    fun removeBackupEntry(entry: BackupMetadata): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(books: List<BookEntity>, backupStorageProvider: BackupStorageProvider): Completable

    fun restoreBackup(
        entry: BackupMetadata,
        bookRepository: BookRepository,
        strategy: RestoreStrategy
    ): Completable
}