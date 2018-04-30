package forpdateam.ru.forpda.presentation.editpost

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.posteditor.PostEditorRepository
import forpdateam.ru.forpda.presentation.theme.ThemeTemplate

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class EditPostPresenter(
        private val editorRepository: PostEditorRepository,
        private val themeTemplate: ThemeTemplate
) : BasePresenter<EditPostView>() {

    private val postForm = EditPostForm()

    fun initPostForm(newPostForm: EditPostForm) {
        postForm.apply {
            postForm.type = newPostForm.type
            postForm.attachments = newPostForm.attachments
            postForm.message = newPostForm.message
            postForm.forumId = newPostForm.forumId
            postForm.topicId = newPostForm.topicId
            postForm.postId = newPostForm.postId
            postForm.st = newPostForm.st
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if (postForm.type == EditPostForm.TYPE_EDIT_POST) {
            loadForm()
        } else {
            viewState.showForm(postForm)
        }
    }

    fun sendMessage(message: String, attachments: List<AttachmentItem>) {
        postForm.message = message
        postForm.attachments.clear()
        for (item in attachments) {
            postForm.addAttachment(item)
        }
        editorRepository
                .sendPost(postForm)
                .map { themeTemplate.mapEntity(it) }
                .subscribe({
                    viewState.onPostSend(it, postForm)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun loadForm() {
        editorRepository
                .loadForm(postForm.postId)
                .subscribe({
                    postForm.message = it.message
                    postForm.editReason = it.editReason
                    postForm.attachments = it.attachments
                    it.poll?.let {
                        postForm.poll = it
                    }
                    viewState.showForm(it)
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        editorRepository
                .uploadFiles(postForm.postId, files, pending)
                .subscribe({
                    viewState.onUploadFiles(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun deleteFiles(items: List<AttachmentItem>) {
        editorRepository
                .deleteFiles(postForm.postId, items)
                .subscribe({
                    viewState.onDeleteFiles(it)
                }, {
                    this.handleErrorRx(it)
                })
                .addToDisposable()
    }

    fun onSendClick() {
        if (postForm.type == EditPostForm.TYPE_EDIT_POST) {
            viewState.showReasonDialog(postForm)
        } else {
            viewState.sendMessage()
        }
    }

    fun onReasonEdit(reason: String) {
        postForm.editReason = reason
        viewState.sendMessage()
    }

}
