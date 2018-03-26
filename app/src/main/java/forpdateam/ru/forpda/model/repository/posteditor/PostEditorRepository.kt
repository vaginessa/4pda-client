package forpdateam.ru.forpda.model.repository.posteditor

import forpdateam.ru.forpda.api.Api
import forpdateam.ru.forpda.api.RequestFile
import forpdateam.ru.forpda.api.mentions.Mentions
import forpdateam.ru.forpda.api.mentions.models.MentionsData
import forpdateam.ru.forpda.api.theme.editpost.EditPost
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm
import forpdateam.ru.forpda.api.theme.models.ThemePage
import forpdateam.ru.forpda.apirx.apiclasses.ThemeRx
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 01.01.18.
 */

class PostEditorRepository(
        private val schedulers: SchedulersProvider,
        private val editPostApi: EditPost
) {

    fun loadForm(postId: Int): Observable<EditPostForm> = Observable
            .fromCallable { editPostApi.loadForm(postId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun uploadFiles(id: Int, files: List<RequestFile>, pending: List<AttachmentItem>): Observable<List<AttachmentItem>> = Observable
            .fromCallable { editPostApi.uploadFilesV2(id, files, pending) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteFiles(id: Int, items: List<AttachmentItem>): Observable<List<AttachmentItem>> = Observable
            .fromCallable { editPostApi.deleteFilesV2(id, items) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendPost(form: EditPostForm): Observable<ThemePage> = Observable
            .fromCallable { editPostApi.sendPost(form) }
            .map { ThemeRx.transform(it, true) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
