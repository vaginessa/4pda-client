package forpdateam.ru.forpda.ui.fragments.qms.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.FilePickHelper;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem;
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser;
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel;
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage;
import forpdateam.ru.forpda.model.data.remote.api.RequestFile;
import forpdateam.ru.forpda.model.repository.temp.TempHelper;
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatPresenter;
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatView;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment implements ChatThemeCreator.ThemeCreatorInterface, ExtendedWebView.JsLifeCycleListener, QmsChatView {
    private final static String LOG_TAG = QmsChatFragment.class.getSimpleName();
    private final static String JS_INTERFACE = "IChat";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_NICK_ARG = "USER_NICK_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String THEME_TITLE_ARG = "THEME_TITLE_ARG";
    private final static Pattern attachmentPattern = Pattern.compile("\\[url=(https:\\/\\/.*?\\.ibb\\.co[^\\]]*?)\\]");

    private MenuItem blackListMenuItem;
    private MenuItem noteMenuItem;
    private MenuItem toDialogsMenuItem;
    private ChatThemeCreator themeCreator;
    private ExtendedWebView webView;
    private FrameLayout chatContainer;
    private ProgressBar progressBar;
    private MessagePanel messagePanel;
    private AttachmentsPopup attachmentsPopup;
    private QmsChatJsInterface jsInterface;

    private Observer chatPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.WEBVIEW_FONT_SIZE: {
                webView.setRelativeFontSize(Preferences.Main.getWebViewSize(getContext()));
            }
        }
    };


    @InjectPresenter
    QmsChatPresenter presenter;

    @ProvidePresenter
    QmsChatPresenter providePresenter() {
        return new QmsChatPresenter(App.get().Di().getQmsRepository());
    }

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        runInUiThread(() -> presenter.handleEvent(event));
    };


    public QmsChatFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_chat));
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            presenter.setUserId(getArguments().getInt(USER_ID_ARG, QmsChatModel.NOT_CREATED));
            presenter.setThemeId(getArguments().getInt(THEME_ID_ARG, QmsChatModel.NOT_CREATED));
            presenter.setTitle(getArguments().getString(THEME_TITLE_ARG));
            presenter.setAvatarUrl(getArguments().getString(USER_AVATAR_ARG));
            presenter.setNick(getArguments().getString(USER_NICK_ARG));
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_qms_chat);
        chatContainer = (FrameLayout) findViewById(R.id.qms_chat_container);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, coordinatorLayout, false);
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        attachWebView(webView);
        chatContainer.addView(webView, 0);
        attachmentsPopup = messagePanel.getAttachmentsPopup();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        jsInterface = new QmsChatJsInterface(presenter);
        webView.setJsLifeCycleListener(this);
        webView.addJavascriptInterface(jsInterface, JS_INTERFACE);
        registerForContextMenu(webView);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
        loadBaseWebContainer();

        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
        attachmentsPopup.setDeleteOnClickListener(v -> {
            attachmentsPopup.preDeleteFiles();
            List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
            for (AttachmentItem item : selectedFiles) {
                item.setStatus(AttachmentItem.STATUS_REMOVED);
            }
            attachmentsPopup.onDeleteFiles(selectedFiles);
        });


        attachmentsPopup.setInsertAttachmentListener(item -> String.format(Locale.getDefault(),
                "\n[url=%s]Файл: %s, Размер: %s, Thumb: %s[/url]\n",
                item.getUrl(),
                item.getName(),
                item.getWeight(),
                item.getImageUrl()));

        messagePanel.addSendOnClickListener(v -> {
            presenter.onSendClick();
        });


        messagePanel.setHeightChangeListener(newHeight -> {
            webView.setPaddingBottom(newHeight);
        });
        App.get().addPreferenceChangeObserver(chatPreferenceObserver);

        if (presenter.getThemeId() == QmsChatModel.NOT_CREATED) {
            themeCreator = new ChatThemeCreator(this, presenter);
        }
    }

    private void addUnusedAttachments() {
        try {
            Matcher matcher = attachmentPattern.matcher(messagePanel.getMessage());
            ArrayList<String> attachmentsUrls = new ArrayList<>();
            while (matcher.find()) {
                attachmentsUrls.add(matcher.group(1));
            }
            ArrayList<AttachmentItem> notAttached = new ArrayList<>();
            for (AttachmentItem item : attachmentsPopup.getAttachments()) {
                if (!attachmentsUrls.contains(item.getUrl())) {
                    notAttached.add(item);
                }
            }
            messagePanel.getMessageField().setSelection(messagePanel.getMessageField().getText().length());
            attachmentsPopup.insertAttachment(notAttached, false);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        blackListMenuItem = menu
                .add(R.string.add_to_blacklist)
                .setOnMenuItemClickListener(item -> {
                    presenter.blockUser();
                    return false;
                });
        noteMenuItem = menu
                .add(R.string.create_note)
                .setOnMenuItemClickListener(item -> {
                    presenter.createThemeNote();
                    return true;
                });
        toDialogsMenuItem = menu
                .add(R.string.to_dialogs)
                .setOnMenuItemClickListener(item -> {
                    presenter.openDialogs();
                    return true;
                });
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            blackListMenuItem.setEnabled(true);
            noteMenuItem.setEnabled(true);
            toDialogsMenuItem.setEnabled(true);
        } else {
            blackListMenuItem.setEnabled(false);
            noteMenuItem.setEnabled(false);
            toDialogsMenuItem.setEnabled(false);
        }
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        progressBar.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
        refreshToolbarMenuItems(!isRefreshing);
    }

    //From theme creator
    @Override
    public void onCreateNewTheme(String nick, String title, String message) {
        addUnusedAttachments();
        presenter.sendNewTheme(nick, title, message);
    }

    @Override
    public void temp_sendMessage() {
        sendMessage();
    }

    @Override
    public void temp_sendNewTheme() {
        themeCreator.sendNewTheme();
    }

    @Override
    public void onShowSearchRes(@NotNull List<? extends ForumUser> res) {
        themeCreator.onShowSearchRes(res);
    }

    @Override
    public void showChat(@NotNull QmsChatModel data) {
        App.get().subscribeQms(notification);
        progressBar.setVisibility(View.GONE);

        MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
        App.setTemplateResStrings(t);
        int end = data.getMessages().size();
        int start = Math.max(end - 30, 0);
        TempHelper.INSTANCE.generateMess(t, data.getMessages(), start, end);
        String messagesSrc = t.generateOutput();
        t.reset();
        data.setShowedMessIndex(start);

        messagesSrc = TempHelper.INSTANCE.transformMessageSrc(messagesSrc);

        Log.d(LOG_TAG, "showNewMess");
        webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));

        refreshToolbarMenuItems(true);
        setTitles(data.getTitle(), data.getNick());
    }

    @Override
    public void setTitles(@NotNull String title, @NotNull String nick) {
        setSubtitle(nick);
        setTitle(title);
        setTabTitle(String.format(getString(R.string.fragment_tab_title_chat), title, nick));
    }

    @Override
    public void onNewThemeCreate(QmsChatModel chat) {
        themeCreator.onNewThemeCreate();
        messagePanel.clearMessage();
        messagePanel.clearAttachments();
    }

    //Chat
    private void loadBaseWebContainer() {
        MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT);
        App.setTemplateResStrings(t);
        t.setVariableOpt("style_type", App.get().getCssStyleType());
        t.setVariableOpt("body_type", "qms");
        t.setVariableOpt("messages", "");
        String html = t.generateOutput();
        t.reset();
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", html, "text/html", "utf-8", null);
    }


    @Override
    public void onNewMessages(@NotNull List<? extends QmsMessage> items) {
        Log.d(LOG_TAG, "Returned messages " + items.size());
        if (!items.isEmpty()) {
            MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
            App.setTemplateResStrings(t);
            for (int i = 0; i < items.size(); i++) {
                QmsMessage message = items.get(i);
                TempHelper.INSTANCE.generateMess(t, message);
            }
            String messagesSrc = t.generateOutput();
            t.reset();
            messagesSrc = TempHelper.INSTANCE.transformMessageSrc(messagesSrc);
            webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));
        }
    }

    @Override
    public void setMessageRefreshing(boolean isRefreshing) {
        messagePanel.setProgressState(isRefreshing);
    }

    @Override
    public void onSentMessage(@NotNull List<? extends QmsMessage> items) {
        if (!items.isEmpty() && items.get(0).getContent() != null) {
            //Empty because result returned from websocket
            messagePanel.clearMessage();
            messagePanel.clearAttachments();
        }
    }

    private void sendMessage() {
        addUnusedAttachments();
        presenter.sendMessage(messagePanel.getMessage());
    }

    @Override
    public void showAvatar(@NotNull String avatarUrl) {
        toolbarImageView.setContentDescription(getString(R.string.user_avatar));
        toolbarImageView.setOnClickListener(view1 -> presenter.openProfile());
        ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView);
        toolbarImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBlockUser(boolean res) {
        if (res) {
            Toast.makeText(getContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showCreateNote(@NotNull String name, @NotNull String nick, @NotNull String url) {
        String title = String.format(getString(R.string.dialog_Title_Nick), name, nick);
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    @Override
    public void showMoreMessages(@NotNull List<? extends QmsMessage> items, int startIndex, int endIndex) {
        MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
        App.setTemplateResStrings(t);
        TempHelper.INSTANCE.generateMess(t, items, startIndex, endIndex);
        String messagesSrc = t.generateOutput();
        t.reset();
        messagesSrc = TempHelper.INSTANCE.transformMessageSrc(messagesSrc);
        webView.evalJs("showMoreMess('" + messagesSrc + "')");
    }

    @Override
    public void makeAllRead() {
        webView.evalJs("makeAllRead();");
    }

    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
    }

    /* ATTACHMENTS LOADER */

    public void uploadFiles(List<RequestFile> files) {
        List<AttachmentItem> pending = attachmentsPopup.preUploadFiles(files);
        presenter.uploadFiles(files, pending);
    }

    @Override
    public void onUploadFiles(@NotNull List<? extends AttachmentItem> items) {
        attachmentsPopup.onUploadFiles(items);
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

    public void tryPickFile() {
        App.get().checkStoragePermission(() -> startActivityForResult(FilePickHelper.pickFile(true), REQUEST_PICK_FILE), App.getActivity());
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        return messagePanel.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        messagePanel.onResume();
        App.get().subscribeQms(notification);
        presenter.checkNewMessages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.get().removePreferenceChangeObserver(chatPreferenceObserver);
        App.get().unSubscribeQms(notification);
        messagePanel.onDestroy();
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.setJsLifeCycleListener(null);
        webView.endWork();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.get().unSubscribeQms(notification);
        messagePanel.onPause();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }
}
