package forpdateam.ru.forpda.presentation.notes

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.model.repository.note.NotesRepository

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class NotesPresenter(
        private val notesRepository: NotesRepository
) : BasePresenter<NotesView>() {


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadNotes()
    }

    fun loadNotes() {
        notesRepository
                .loadNotes()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showNotes(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun deleteNote(id: Long) {
        notesRepository
                .deleteNote(id)
                .subscribe({
                    loadNotes()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun addNote(item: NoteItem) {
        notesRepository
                .addNote(item)
                .subscribe({
                    loadNotes()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun addNotes(items: List<NoteItem>) {
        notesRepository
                .addNotes(items)
                .subscribe({
                    loadNotes()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun exportNotes() {
        notesRepository
                .exportNotes()
                .subscribe({
                    viewState.onExportNotes(it)
                },{
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun importNotes(jsonSource: String) {
        notesRepository
                .importNotes(jsonSource)
                .subscribe({
                    viewState.onImportNotes()
                    loadNotes()
                },{
                    it.printStackTrace()
                })
                .addToDisposable()
    }


    fun onItemClick(item: NoteItem) {
        IntentHandler.handle(item.link)
    }

    fun copyLink(item: NoteItem) {
        Utils.copyToClipBoard(item.link)
    }

    fun editNote(item: NoteItem) {
        viewState.showNotesEditPopup(item)
    }

    fun addNote() {
        viewState.showNotesAddPopup()
    }

}
