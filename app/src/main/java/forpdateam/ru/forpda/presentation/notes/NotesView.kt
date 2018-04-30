package forpdateam.ru.forpda.presentation.notes

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.notes.NoteItem

/**
 * Created by radiationx on 01.01.18.
 */

interface NotesView : IBaseView {
    fun showNotes(items: List<NoteItem>)
    fun showNotesEditPopup(item: NoteItem)
    fun showNotesAddPopup()
    fun onImportNotes()
    fun onExportNotes(paht:String)
}
