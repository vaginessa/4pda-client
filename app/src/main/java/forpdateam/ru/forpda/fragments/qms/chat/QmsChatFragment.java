package forpdateam.ru.forpda.fragments.qms.chat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.rxapi.apiclasses.QmsRx;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.FilePickHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ExtendedWebView;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.attachments.AttachmentsPopup;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment implements ChatThemeCreator.ThemeCreatorInterface, ExtendedWebView.JsLifeCycleListener {
    private final static String LOG_TAG = QmsChatFragment.class.getSimpleName();
    private final static String JS_INTERFACE = "IChat";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_NICK_ARG = "USER_NICK_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String THEME_TITLE_ARG = "THEME_TITLE_ARG";
    private final static Pattern attachmentPattern = Pattern.compile("\\[url=https?:\\/\\/savepic\\.net\\/(\\d+)\\.[^\\]]*?\\]");

    private MenuItem blackListMenuItem;
    final QmsChatModel currentChat = new QmsChatModel();
    private ChatThemeCreator themeCreator;
    private ExtendedWebView webView;
    private FrameLayout chatContainer;
    private ProgressBar progressBar;
    private MessagePanel messagePanel;
    private AttachmentsPopup attachmentsPopup;

    private Subscriber<QmsChatModel> mainSubscriber = new Subscriber<>(this);
    private Subscriber<ArrayList<QmsMessage>> messageSubscriber = new Subscriber<>(this);
    private Subscriber<ArrayList<QmsContact>> contactsSubscriber = new Subscriber<>(this);

    private Observer chatPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.WEBVIEW_FONT_SIZE: {
                webView.setRelativeFontSize(Preferences.Main.getWebViewSize());
            }
        }
    };


    private WebSocket webSocket;

    private WebSocketListener webSocketListener = new WebSocketListener() {
        Pattern pattern = Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]");

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d(LOG_TAG, "ON OPEN: " + response.toString());
            webSocket.send("[0,\"sv\"]");
            webSocket.send("[0, \"ea\", \"u" + ClientHelper.getUserId() + "\"]");
        }


        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(LOG_TAG, "ON T MESSAGE: " + text);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                int themeId = Integer.parseInt(matcher.group(4));
                int eventCode = Integer.parseInt(matcher.group(5));
                int messageId = Integer.parseInt(matcher.group(6));
                if (themeId == currentChat.getThemeId()) {
                    if (eventCode == 1) {
                        Log.d(LOG_TAG, "NEW QMS MESSAGE " + themeId + " : " + messageId);
                        onNewWsMessage(themeId, messageId);
                    } else if (eventCode == 2) {
                        Log.d(LOG_TAG, "THREAD READED");
                        webView.evalJs("makeAllRead();");
                    }
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d(LOG_TAG, "ON B MESSAGE: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d(LOG_TAG, "ON CLOSING: " + code + " " + reason);
            webSocket.close(1000, null);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.d(LOG_TAG, "ON CLOSED: " + code + " " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d(LOG_TAG, "ON FAILURE: " + t.getMessage() + " " + response);
            t.printStackTrace();
        }
    };

    public QmsChatFragment() {
        configuration.setDefaultTitle("Чат");
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentChat.setUserId(getArguments().getInt(USER_ID_ARG, QmsChatModel.NOT_CREATED));
            currentChat.setThemeId(getArguments().getInt(THEME_ID_ARG, QmsChatModel.NOT_CREATED));
            currentChat.setTitle(getArguments().getString(THEME_TITLE_ARG));
            currentChat.setAvatarUrl(getArguments().getString(USER_AVATAR_ARG));
            currentChat.setNick(getArguments().getString(USER_NICK_ARG));
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
        messagePanel.setHeightChangeListener(newHeight -> {
            webView.setPaddingBottom(newHeight);
        });

        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        webView.setJsLifeCycleListener(this);

        chatContainer.addView(webView, 0);
        webView.addJavascriptInterface(this, JS_INTERFACE);
        registerForContextMenu(webView);
        webView.setWebViewClient(new QmsWebViewClient());
        loadBaseWebContainer();

        attachmentsPopup = messagePanel.getAttachmentsPopup();
        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
        attachmentsPopup.setDeleteOnClickListener(v -> {
            attachmentsPopup.preDeleteFiles();
            List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
            for (AttachmentItem item : selectedFiles) {
                item.setStatus(AttachmentItem.STATUS_REMOVED);
            }
            attachmentsPopup.onDeleteFiles(selectedFiles);
        });
        attachmentsPopup.setInsertAttachmentListener(item -> "\n[url=http://savepic.net/" + item.getId() + "." + item.getExtension() + "]" +
                "Файл: " + item.getName() + ", Размер: " + item.getWeight() + ", ID: " + item.getId() + "[/url]");
        messagePanel.addSendOnClickListener(v -> {
            if (currentChat.getThemeId() == QmsChatModel.NOT_CREATED) {
                themeCreator.sendNewTheme();
            } else {
                sendMessage();
            }
        });


        viewsReady();
        App.getInstance().addPreferenceChangeObserver(chatPreferenceObserver);
        tryShowAvatar();

        if (currentChat.getNick() != null) {
            setSubtitle(currentChat.getNick());
        }
        if (currentChat.getTitle() != null) {
            setTitle(currentChat.getTitle());
        }
        if (currentChat.getThemeId() == QmsChatModel.NOT_CREATED) {
            themeCreator = new ChatThemeCreator(this);
        }

        return view;
    }

    private void addUnusedAttachments() {
        try {
            Matcher matcher = attachmentPattern.matcher(messagePanel.getMessage());
            ArrayList<Integer> attachmentsIds = new ArrayList<>();
            while (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                attachmentsIds.add(id);
            }
            ArrayList<AttachmentItem> notAttached = new ArrayList<>();
            for (AttachmentItem item : attachmentsPopup.getAttachments()) {
                if (!attachmentsIds.contains(item.getId())) {
                    notAttached.add(item);
                }
            }
            messagePanel.getMessageField().setSelection(messagePanel.getMessageField().getText().length());
            attachmentsPopup.insertAttachment(notAttached, false);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        blackListMenuItem = getMenu().add("В черный список")
                .setOnMenuItemClickListener(item -> {
                    contactsSubscriber.subscribe(RxApi.Qms().blockUser(currentChat.getNick()), qmsContacts -> {
                        if (qmsContacts.size() > 0) {
                            Toast.makeText(getContext(), "Пользователь добавлен в черный список", Toast.LENGTH_SHORT).show();
                        }
                    }, new ArrayList<>());
                    return false;
                });
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            blackListMenuItem.setEnabled(true);
        } else {
            blackListMenuItem.setEnabled(false);
        }
    }

    //From theme creator
    @Override
    public void onCreateNewTheme(String nick, String title, String message) {
        addUnusedAttachments();
        refreshToolbarMenuItems(false);
        progressBar.setVisibility(View.VISIBLE);
        mainSubscriber.subscribe(RxApi.Qms().sendNewTheme(nick, title, message), this::onNewThemeCreate, new QmsChatModel());
    }

    @Override
    public void loadData() {
        if (currentChat.getUserId() != QmsChatModel.NOT_CREATED && currentChat.getThemeId() != QmsChatModel.NOT_CREATED) {
            refreshToolbarMenuItems(false);
            progressBar.setVisibility(View.VISIBLE);
            mainSubscriber.subscribe(RxApi.Qms().getChat(currentChat.getUserId(), currentChat.getThemeId()), this::onLoadChat, new QmsChatModel(), v -> loadData());
        }
    }

    private void onNewThemeCreate(QmsChatModel chat) {
        themeCreator.onNewThemeCreate();
        messagePanel.clearMessage();
        messagePanel.clearAttachments();
        onLoadChat(chat);
    }

    //Chat
    private void loadBaseWebContainer() {
        MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT);
        t.setVariableOpt("style_type", App.getInstance().getCssStyleType());
        t.setVariableOpt("body_type", "qms");
        t.setVariableOpt("messages", "");
        String html = t.generateOutput();
        t.reset();
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", html, "text/html", "utf-8", null);
    }


    private void onLoadChat(QmsChatModel loadedChat) {
        progressBar.setVisibility(View.GONE);
        if (webSocket == null)
            webSocket = Client.getInstance().createWebSocketConnection(webSocketListener);
        currentChat.setThemeId(loadedChat.getThemeId());
        currentChat.setTitle(loadedChat.getTitle());
        currentChat.setUserId(loadedChat.getUserId());
        currentChat.setNick(loadedChat.getNick());
        currentChat.getMessages().addAll(loadedChat.getMessages());

        MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
        int end = currentChat.getMessages().size();
        int start = Math.max(end - 30, 0);
        QmsRx.generateMess(t, currentChat.getMessages(), start, end);
        String messagesSrc = t.generateOutput();
        t.reset();
        currentChat.setShowedMessIndex(start);
        messagesSrc = messagesSrc.replaceAll("\n", "").replaceAll("'", "&apos;");
        final String finalMessagesSrc = messagesSrc;

        Log.d(LOG_TAG, "showNewMess");
        webView.evalJs("showNewMess('".concat(finalMessagesSrc).concat("', true)"));

        refreshToolbarMenuItems(true);
        if (currentChat.getNick() != null) {
            setSubtitle(currentChat.getNick());
        }
        if (currentChat.getTitle() != null) {
            setTitle(currentChat.getTitle());
        }
    }


    private void onNewWsMessage(int themeId, int messageId) {
        messageSubscriber.subscribe(RxApi.Qms().getMessagesFromWs(themeId, messageId, currentChat.getMessages().get(currentChat.getMessages().size() - 1).getId()), qmsMessage -> {
            Log.d(LOG_TAG, "Returned messages " + qmsMessage.size());
            if (qmsMessage.size() > 0) {
                MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
                for (int i = 0; i < qmsMessage.size(); i++) {
                    QmsMessage message = qmsMessage.get(i);
                    for (QmsMessage viewmessage : currentChat.getMessages()) {
                        if (viewmessage.getId() == message.getId()) {
                            return;
                        }
                    }
                    currentChat.addMessage(message);
                    QmsRx.generateMess(t, message);
                }
                String messagesSrc = t.generateOutput();
                t.reset();
                messagesSrc = messagesSrc.replaceAll("\n", "").replaceAll("'", "&apos;");
                webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));
            }

        }, new ArrayList<>());
    }


    private void sendMessage() {
        messagePanel.setProgressState(true);
        addUnusedAttachments();
        messageSubscriber.subscribe(RxApi.Qms().sendMessage(currentChat.getUserId(), currentChat.getThemeId(), messagePanel.getMessage()), qmsMessage -> {
            messagePanel.setProgressState(false);
            if (qmsMessage.size() > 0 && qmsMessage.get(0).getContent() != null) {
                //Empty because result returned from websocket
                messagePanel.clearMessage();
                messagePanel.clearAttachments();
            }
        }, new ArrayList<>());
    }


    private void tryShowAvatar() {
        if (currentChat.getAvatarUrl() != null && currentChat.getUserId() != QmsChatModel.NOT_CREATED) {
            ImageLoader.getInstance().displayImage(currentChat.getAvatarUrl(), toolbarImageView);
            toolbarImageView.setVisibility(View.VISIBLE);
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + currentChat.getUserId()));
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }

    @JavascriptInterface
    public void showMoreMess() {
        MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
        int endIndex = currentChat.getShowedMessIndex();
        int startIndex = Math.max(endIndex - 30, 0);
        currentChat.setShowedMessIndex(startIndex);
        QmsRx.generateMess(t, currentChat.getMessages(), startIndex, endIndex);
        String messagesSrc = t.generateOutput();
        messagesSrc = messagesSrc.replaceAll("\n", "");
        t.reset();
        webView.evalJs("showMoreMess('" + messagesSrc + "')");
    }

    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
    }

    private class QmsWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUri(Uri.parse(url));
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUri(request.getUrl());
        }

        private boolean handleUri(Uri uri) {
            IntentHandler.handle(uri.toString());
            return true;
        }
    }

    /* ATTACHMENTS LOADER */

    private Subscriber<List<AttachmentItem>> attachmentSubscriber = new Subscriber<>(this);

    public void uploadFiles(List<RequestFile> files) {
        List<AttachmentItem> pending = attachmentsPopup.preUploadFiles(files);
        attachmentSubscriber.subscribe(RxApi.Qms().uploadFiles(files, pending), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
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
        App.getInstance().checkStoragePermission(() -> startActivityForResult(FilePickHelper.pickImage(true), REQUEST_PICK_FILE), App.getActivity());
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getInstance().removePreferenceChangeObserver(chatPreferenceObserver);
        if (webSocket != null)
            webSocket.close(1000, null);
        messagePanel.onDestroy();
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.setJsLifeCycleListener(null);
        webView.destroy();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    @Override
    public void onPause() {
        super.onPause();
        messagePanel.onPause();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }
}
