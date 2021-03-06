package forpdateam.ru.forpda.ui.fragments.theme.editpost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.FilePickHelper;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragment;
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup;

import static forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm.ARG_TYPE;
import static forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm.ERROR_NONE;
import static forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm.TYPE_EDIT_POST;
import static forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm.TYPE_NEW_POST;

/**
 * Created by radiationx on 14.01.17.
 */

public class EditPostFragment extends TabFragment {
    private final static String ARG_THEME_NAME = "theme_name";
    private final static String ARG_ATTACHMENTS = "attachments";
    private final static String ARG_MESSAGE = "message";
    private final static String ARG_FORUM_ID = "forumId";
    private final static String ARG_TOPIC_ID = "topicId";
    private final static String ARG_POST_ID = "postId";
    private final static String ARG_ST = "st";

    private final EditPostForm postForm = new EditPostForm();
    private MessagePanel messagePanel;
    private EditPollPopup pollPopup;


    public static EditPostFragment newInstance(int postId, int topicId, int forumId, int st, String themeName) {
        Bundle args = new Bundle();
        if (themeName != null)
            args.putString(ARG_THEME_NAME, themeName);
        args.putInt(ARG_TYPE, TYPE_EDIT_POST);
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
        args.putInt(ARG_TYPE, TYPE_NEW_POST);
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
            postForm.setType(args.getInt(ARG_TYPE));
            postForm.setAttachments(args.getParcelableArrayList(ARG_ATTACHMENTS));
            postForm.setMessage(args.getString(ARG_MESSAGE));
            postForm.setForumId(args.getInt(ARG_FORUM_ID));
            postForm.setTopicId(args.getInt(ARG_TOPIC_ID));
            postForm.setPostId(args.getInt(ARG_POST_ID));
            postForm.setSt(args.getInt(ARG_ST));
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
        viewsReady();
        messagePanel.addSendOnClickListener(v -> {
            if (postForm.getType() == TYPE_EDIT_POST) {
                showReasonDialog();
            } else {
                sendMessage();
            }
        });
        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
        attachmentsPopup.setDeleteOnClickListener(v -> removeFiles());
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(ARG_THEME_NAME);
            setTitle((App.get().getString(postForm.getType() == TYPE_NEW_POST ? R.string.editpost_title_answer : R.string.editpost_title_edit)).concat(" ").concat(title != null ? title : ""));
        }
        messagePanel.getEditPollButton().setOnClickListener(v -> {
            if (pollPopup != null)
                pollPopup.show();
        });
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        /*getMenu().add("prnt")
                .setOnMenuItemClickListener(item -> {
                    EditPoll poll = postForm.getPoll();
                    EditPost.printPoll(poll);
                    return true;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);*/
    }

    @Override
    public boolean loadData() {
        if(!super.loadData()){
            return false;
        }
        if (postForm.getType() == TYPE_EDIT_POST) {
            loadForm();
        } else {
            messagePanel.insertText(postForm.getMessage());
            messagePanel.getAttachmentsPopup().onLoadAttachments(postForm);
            messagePanel.getMessageField().requestFocus();
            getMainActivity().showKeyboard(messagePanel.getMessageField());
        }
        return true;
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
    public boolean onBackPressed() {
        super.onBackPressed();
        if (messagePanel.onBackPressed())
            return true;

        if (showExitDialog()) {
            return true;
        }

        //Синхронизация с полем в фрагменте темы
        TabFragment fragment = TabManager.get().get(getParentTag());
        if (fragment != null && fragment instanceof ThemeFragment) {
            ThemeFragment themeFragment = (ThemeFragment) fragment;
            showSyncDialog(themeFragment);
            return true;
        }
        return false;
    }

    private boolean showExitDialog() {
        if (postForm.getType() == TYPE_EDIT_POST) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.editpost_lose_changes)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        TabManager.get().remove(EditPostFragment.this);
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
            return true;
        }
        return false;
    }

    private void showSyncDialog(ThemeFragment themeFragment) {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.editpost_sync)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    themeFragment.getMessagePanel().setText(messagePanel.getMessage());
                    int[] selectionRange = messagePanel.getSelectionRange();
                    themeFragment.getMessagePanel().getMessageField().setSelection(selectionRange[0], selectionRange[1]);
                    themeFragment.getAttachmentsPopup().setAttachments(messagePanel.getAttachments());
                    TabManager.get().remove(EditPostFragment.this);
                })
                .setNegativeButton(R.string.no, ((dialog, which) -> {
                    if (!showExitDialog()) {
                        TabManager.get().remove(EditPostFragment.this);
                    }
                }))
                .show();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }

    private AttachmentsPopup attachmentsPopup;

    private void sendMessage() {
        messagePanel.setProgressState(true);
        postForm.setMessage(messagePanel.getMessage());
        List<AttachmentItem> attachments = messagePanel.getAttachments();
        postForm.getAttachments().clear();
        for (AttachmentItem item : attachments) {
            postForm.addAttachment(item);
        }
        subscribe(RxApi.EditPost().sendPost(postForm), s -> {
            messagePanel.setProgressState(false);
            if (s.getId() != 0) {
                TabFragment fragment = TabManager.get().get(getParentTag());
                if (fragment != null) {
                    if (fragment instanceof ThemeFragment) {
                        ThemeFragment themeFragment = (ThemeFragment) fragment;
                        if (postForm.getType() == TYPE_EDIT_POST) {
                            themeFragment.onEditPostCompleted(s);
                        } else {
                            themeFragment.onSendPostCompleted(s);
                        }
                    }
                }
                TabManager.get().remove(EditPostFragment.this);
            }

        }, new ThemePage(), v -> loadData());
    }

    private void loadForm() {
        messagePanel.getFormProgress().setVisibility(View.VISIBLE);
        messagePanel.getMessageField().setVisibility(View.GONE);
        subscribe(RxApi.EditPost().loadForm(postForm.getPostId()), form -> {
            messagePanel.getMessageField().setVisibility(View.VISIBLE);
            messagePanel.getFormProgress().setVisibility(View.GONE);
            if (form.getErrorCode() != ERROR_NONE) {
                Toast.makeText(getContext(), R.string.editpost_error_edit, Toast.LENGTH_SHORT).show();
                TabManager.get().remove(EditPostFragment.this);
                return;
            }
            postForm.setMessage(form.getMessage());
            postForm.setEditReason(form.getEditReason());
            postForm.setAttachments(form.getAttachments());
            if (form.getPoll() != null) {
                postForm.setPoll(form.getPoll());

                pollPopup = new EditPollPopup(getContext());
                pollPopup.setPoll(postForm.getPoll());
                messagePanel.getEditPollButton().setVisibility(View.VISIBLE);
            } else {
                messagePanel.getEditPollButton().setVisibility(View.GONE);
            }

            attachmentsPopup.onLoadAttachments(form);
            messagePanel.insertText(postForm.getMessage());
            messagePanel.getMessageField().requestFocus();
            getMainActivity().showKeyboard(messagePanel.getMessageField());
        }, postForm, null);
    }

    public void uploadFiles(List<RequestFile> files) {
        List<AttachmentItem> pending = attachmentsPopup.preUploadFiles(files);
        subscribe(RxApi.EditPost().uploadFiles(postForm.getPostId(), files, pending), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
    }

    public void removeFiles() {
        attachmentsPopup.preDeleteFiles();
        List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
        subscribe(RxApi.EditPost().deleteFiles(postForm.getPostId(), selectedFiles), item -> attachmentsPopup.onDeleteFiles(selectedFiles), selectedFiles, null);
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

    private void showReasonDialog() {
        View view = View.inflate(getContext(), R.layout.edit_post_reason, null);
        EditText editText = (EditText) view.findViewById(R.id.edit_post_reason_field);
        editText.setText(postForm.getEditReason());

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.editpost_reason)
                .setView(view)
                .setPositiveButton(R.string.send, (dialog1, which) -> {
                    postForm.setEditReason(editText.getText().toString());
                    sendMessage();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

}
