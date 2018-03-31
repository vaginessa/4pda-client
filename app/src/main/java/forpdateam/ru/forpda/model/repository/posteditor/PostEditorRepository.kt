package forpdateam.ru.forpda.model.repository.posteditor

import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import io.reactivex.Observable

/**
 * Created by radiationx on 01.01.18.
 */

class PostEditorRepository(
        private val schedulers: SchedulersProvider,
        private val editPostApi: EditPostApi
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
            .map { TempHelper.transform(it, true) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
