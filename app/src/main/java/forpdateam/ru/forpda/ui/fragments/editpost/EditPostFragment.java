package forpdateam.ru.forpda.ui.fragments.editpost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.FilePickHelper;
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem;
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm;
import forpdateam.ru.forpda.entity.remote.theme.ThemePage;
import forpdateam.ru.forpda.model.data.remote.api.RequestFile;
import forpdateam.ru.forpda.presentation.editpost.EditPostPresenter;
import forpdateam.ru.forpda.presentation.editpost.EditPostView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragment;
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup;

/**
 * Created by radiationx on 14.01.17.
 */

public class EditPostFragment extends TabFragment implements EditPostView {
    private final static String ARG_THEME_NAME = "theme_name";
    private final static String ARG_ATTACHMENTS = "attachments";
    private final static String ARG_MESSAGE = "message";
    private final static String ARG_FORUM_ID = "forumId";
    private final static String ARG_TOPIC_ID = "topicId";
    private final static String ARG_POST_ID = "postId";
    private final static String ARG_ST = "st";

    private int formType = 0;

    private MessagePanel messagePanel;
    private EditPollPopup pollPopup;
    private AttachmentsPopup attachmentsPopup;

    @InjectPresenter
    EditPostPresenter presenter;

    @ProvidePresenter
    EditPostPresenter providePresenter() {
        return new EditPostPresenter(
                App.get().Di().getEditPostRepository(),
                App.get().Di().getThemeTemplate(),
                App.get().Di().getRouter()
        );
    }

    public static EditPostFragment newInstance(int postId, int topicId, int forumId, int st, String themeName) {
        Bundle args = new Bundle();
        if (themeName != null)
            args.putString(ARG_THEME_NAME, themeName);
        args.putInt(EditPostForm.ARG_TYPE, EditPostForm.TYPE_EDIT_POST);
        args.putInt(ARG_FORUM_ID, forumId);
        args.putInt(ARG_TOPIC_ID, topicId);
        args.putInt(ARG_POST_ID, postId);
        args.putInt(ARG_ST, st);
        EditPostFragment fragment = new EditPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static EditPostFragment newInstance(EditPostForm form, String themeName) {
        Bundle args = new Bundle();
        if (themeName != null)
            args.putString(ARG_THEME_NAME, themeName);
        args.putInt(EditPostForm.ARG_TYPE, EditPostForm.TYPE_NEW_POST);
        args.putParcelableArrayList(ARG_ATTACHMENTS, form.getAttachments());
        args.putString(ARG_MESSAGE, form.getMessage());
        args.putInt(ARG_FORUM_ID, form.getForumId());
        args.putInt(ARG_TOPIC_ID, form.getTopicId());
        args.putInt(ARG_POST_ID, form.getPostId());
        args.putInt(ARG_ST, form.getSt());
        EditPostFragment fragment = new EditPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            EditPostForm postForm = new EditPostForm();
            formType = args.getInt(EditPostForm.ARG_TYPE);
            postForm.setType(formType);
            postForm.setAttachments(args.getParcelableArrayList(ARG_ATTACHMENTS));
            postForm.setMessage(args.getString(ARG_MESSAGE));
            postForm.setForumId(args.getInt(ARG_FORUM_ID));
            postForm.setTopicId(args.getInt(ARG_TOPIC_ID));
            postForm.setPostId(args.getInt(ARG_POST_ID));
            postForm.setSt(args.getInt(ARG_ST));
            presenter.initPostForm(postForm);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, fragmentContent, true);
        attachmentsPopup = messagePanel.getAttachmentsPopup();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messagePanel.addSendOnClickListener(v -> presenter.onSendClick());
        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
        attachmentsPopup.setDeleteOnClickListener(v -> removeFiles());
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(ARG_THEME_NAME);
            setTitle((App.get().getString(formType == EditPostForm.TYPE_NEW_POST ? R.string.editpost_title_answer : R.string.editpost_title_edit)).concat(" ").concat(title != null ? title : ""));
        }
        messagePanel.getEditPollButton().setOnClickListener(v -> {
            if (pollPopup != null)
                pollPopup.show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        messagePanel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        messagePanel.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messagePanel.onDestroy();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        if (messagePanel.onBackPressed())
            return true;

        if (showExitDialog()) {
            return true;
        }

        //Синхронизация с полем в фрагменте темы
        if (formType == EditPostForm.TYPE_NEW_POST) {
            showSyncDialog();
            return true;
        }
        return false;
    }

    public void tryPickFile() {
        App.get().checkStoragePermission(() -> startActivityForResult(FilePickHelper.pickFile(false), REQUEST_PICK_FILE), App.getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            uploadFiles(FilePickHelper.onActivityResult(getContext(), data));
        }
    }

    @Override
    public void showForm(@NotNull EditPostForm form) {
        if (form.getErrorCode() != EditPostForm.ERROR_NONE) {
            Toast.makeText(getContext(), R.string.editpost_error_edit, Toast.LENGTH_SHORT).show();
            presenter.exit();
            return;
        }

        if (form.getPoll() != null) {
            pollPopup = new EditPollPopup(getContext());
            pollPopup.setPoll(form.getPoll());
            messagePanel.getEditPollButton().setVisibility(View.VISIBLE);
        } else {
            messagePanel.getEditPollButton().setVisibility(View.GONE);
        }

        attachmentsPopup.onLoadAttachments(form);
        messagePanel.insertText(form.getMessage());
        messagePanel.getMessageField().requestFocus();
        getMainActivity().showKeyboard(messagePanel.getMessageField());
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        messagePanel.getFormProgress().setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
        messagePanel.getMessageField().setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
        messagePanel.getFormProgress().setVisibility(View.GONE);
    }

    @Override
    public void setSendRefreshing(boolean isRefreshing) {
        messagePanel.setProgressState(isRefreshing);
    }

    @Override
    public void sendMessage() {
        presenter.sendMessage(messagePanel.getMessage(), messagePanel.getAttachments());
    }

    @Override
    public void onPostSend(@NotNull ThemePage page, @NotNull EditPostForm form) {
        presenter.exitWithPage(page);
    }


    public void uploadFiles(List<RequestFile> files) {
        List<AttachmentItem> pending = attachmentsPopup.preUploadFiles(files);
        presenter.uploadFiles(files, pending);
    }

    public void removeFiles() {
        attachmentsPopup.preDeleteFiles();
        List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
        presenter.deleteFiles(selectedFiles);
    }

    @Override
    public void onUploadFiles(@NotNull List<? extends AttachmentItem> items) {
        attachmentsPopup.onUploadFiles(items);
    }

    @Override
    public void onDeleteFiles(@NotNull List<? extends AttachmentItem> items) {
        attachmentsPopup.onDeleteFiles(items);
    }


    @Override
    public void showReasonDialog(@NotNull EditPostForm form) {
        View view = View.inflate(getContext(), R.layout.edit_post_reason, null);
        EditText editText = (EditText) view.findViewById(R.id.edit_post_reason_field);
        editText.setText(form.getEditReason());

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.editpost_reason)
                .setView(view)
                .setPositiveButton(R.string.send, (dialog1, which) -> {
                    presenter.onReasonEdit(editText.getText().toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean showExitDialog() {
        if (formType == EditPostForm.TYPE_EDIT_POST) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.editpost_lose_changes)
                    .setPositiveButton(R.string.ok, (dialog, which) -> presenter.exit())
                    .setNegativeButton(R.string.no, null)
                    .show();
            return true;
        }
        return false;
    }

    private void showSyncDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.editpost_sync)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    int[] selectionRange = messagePanel.getSelectionRange();
                    presenter.exitWithSync(
                            messagePanel.getMessage(),
                            selectionRange,
                            messagePanel.getAttachments()
                    );
                })
                .setNegativeButton(R.string.no, ((dialog, which) -> {
                    if (!showExitDialog()) {
                        presenter.exit();
                    }
                }))
                .show();
    }

}
