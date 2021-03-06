package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_backup_entry.*

/**
 * Author:  Martin Macheiner
 * Date:    22.04.2017
 */
class BackupEntryAdapter(
    ctx: Context,
    onItemClickListener: OnItemClickListener<BackupMetadataState>,
    private val onItemOverflowMenuClickedListener: OnBackupOverflowItemListener
) : BaseAdapter<BackupMetadataState>(ctx, onItemClickListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BackupMetadataState> {
        return BackupViewHolder(inflater.inflate(R.layout.item_backup_entry, parent, false))
    }

    fun updateData(freshData: List<BackupMetadataState>) {
        data.clear()
        data.addAll(freshData)
        notifyDataSetChanged()
    }

    inner class BackupViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<BackupMetadataState>(containerView), LayoutContainer {

        override fun bindToView(content: BackupMetadataState, position: Int) {

            with(content.entry) {
                item_backup_entry_imgview_provider.setImageResource(storageProvider.icon)

                item_backup_entry_txt_time.text = DanteUtils.formatTimestamp(timestamp)
                item_backup_entry_txt_books.text = context.getString(R.string.books_amount, books)
                item_backup_entry_txt_device.text = device

                if (content is BackupMetadataState.Active) {

                    item_backup_entry_card.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    item_backup_entry_btn_overflow.setVisible(true)
                    setupOverflowMenu(content)
                } else {
                    item_backup_entry_card.setBackgroundColor(ContextCompat.getColor(context, R.color.disabled_view))
                    item_backup_entry_btn_overflow.visibility = View.INVISIBLE
                    item_backup_entry_btn_overflow.setOnClickListener(null)
                }
            }
        }

        private fun setupOverflowMenu(content: BackupMetadataState) {

            val entry = content.entry
            val popupMenu = PopupMenu(context, item_backup_entry_btn_overflow)

            popupMenu.menuInflater.inflate(R.menu.menu_backup_item_overflow, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_backup_delete -> {
                        onItemOverflowMenuClickedListener.onBackupItemDeleted(entry, getLocation(content))
                    }
                    R.id.menu_backup_export_request -> {
                        if (entry is BackupMetadata.WithLocalFile) {
                            onItemOverflowMenuClickedListener.onBackupItemDownloadRequest(entry)
                        }
                    }
                    R.id.menu_backup_open_request -> {
                        if (entry is BackupMetadata.WithLocalFile) {
                            onItemOverflowMenuClickedListener.onBackupItemOpenFileRequest(entry)
                        }
                    }
                }
                true
            }

            val showExportOption = content.isFileExportable && entry is BackupMetadata.WithLocalFile
            popupMenu.menu.findItem(R.id.menu_backup_open_request)?.isVisible = showExportOption
            popupMenu.menu.findItem(R.id.menu_backup_export_request)?.isVisible = showExportOption

            val menuHelper = MenuPopupHelper(context, popupMenu.menu as MenuBuilder, item_backup_entry_btn_overflow)
            menuHelper.setForceShowIcon(true)

            item_backup_entry_btn_overflow.setOnClickListener { menuHelper.show() }
        }
    }
}
