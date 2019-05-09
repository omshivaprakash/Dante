package at.shockbytes.dante.backup.provider.shockbytes.api

import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Author:  Martin Macheiner
 * Date:    09.05.2019
 */
interface ShockbytesHerokuApi {

    @GET("backups")
    fun listBackups(
        @Header("Authorization") bearerToken: String
    ): Single<List<BackupEntry>>

    @DELETE("backups")
    fun removeAllBackups(
        @Header("Authorization") bearerToken: String
    ): Completable

    @GET("backup/{backupId}")
    fun getBooksBackupById(
        @Header("Authorization") bearerToken: String,
        @Path("backupId") backupId: String
    ): Single<List<BookEntity>>

    @DELETE("backup/{backupId}")
    fun removeBackupById(
        @Header("Authorization") bearerToken: String,
        @Path("backupId") backupId: String
    ): Completable

    @PUT("backup")
    fun makeBackup(
        @Header("Authorization") bearerToken: String,
        @Body books: List<BookEntity>
    ): Single<BackupEntry>

    companion object {
        const val SERVICE_ENDPOINT = "https://shockbytes-dante.herokuapp.com/"
    }
}