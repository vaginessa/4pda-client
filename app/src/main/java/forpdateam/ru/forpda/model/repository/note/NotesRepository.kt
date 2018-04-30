package forpdateam.ru.forpda.model.repository.note

import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.notes.NotesCache
import forpdateam.ru.forpda.model.data.storage.ExternalStorageProvider
import io.reactivex.Completable
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NotesRepository(
        private val schedulers: SchedulersProvider,
        private val notesCache: NotesCache,
        private val externalStorage: ExternalStorageProvider
) {

    fun loadNotes(): Observable<List<NoteItem>> = Observable
            .fromCallable { notesCache.getItems() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    fun deleteNote(id: Long): Completable = Completable
            .fromCallable { notesCache.delete(id) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun updateNote(item: NoteItem): Completable = Completable
            .fromCallable { notesCache.update(item) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun addNote(item: NoteItem): Completable = Completable
            .fromCallable { notesCache.add(item) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun addNotes(items: List<NoteItem>): Completable = Completable
            .fromCallable { notesCache.add(items) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    fun importNotes(jsonSource: String) = Observable
            .fromCallable {
                val jsonBody = JSONArray(jsonSource)
                val noteItems = mutableListOf<NoteItem>()
                for (i in 0 until jsonBody.length()) {
                    try {
                        val jsonItem = jsonBody.getJSONObject(i)
                        noteItems.add(NoteItem().apply {
                            id = jsonItem.getLong("id")
                            title = jsonItem.getString("title")
                            link = jsonItem.getString("link")
                            content = jsonItem.getString("content")
                        })
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                return@fromCallable noteItems
            }
            .doOnNext { notesCache.add(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun exportNotes() = Observable
            .fromCallable {
                val jsonBody = JSONArray()
                notesCache.getItems().forEach {
                    try {
                        jsonBody.put(JSONObject().apply {
                            put("id", it.id)
                            put("title", it.title)
                            put("link", it.link)
                            put("content", it.content)
                        })
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                val date = SimpleDateFormat("MMddyyy-HHmmss", Locale.getDefault()).format(Date(System.currentTimeMillis()))
                val fileName = "ForPDA_Notes_$date.json"
                externalStorage.saveTextDefault(jsonBody.toString(), fileName)

            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}