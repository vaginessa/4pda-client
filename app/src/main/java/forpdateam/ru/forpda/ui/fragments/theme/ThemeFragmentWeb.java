package forpdateam.ru.forpda.ui.fragments.theme;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.entity.remote.IBaseForumPost;
import forpdateam.ru.forpda.entity.remote.theme.ThemePage;
import forpdateam.ru.forpda.presentation.theme.ThemeJsInterface;
import forpdateam.ru.forpda.presentation.theme.ThemePresenter;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;

/**
 * Created by radiationx on 20.10.16.
 */

public class ThemeFragmentWeb extends ThemeFragment implements ExtendedWebView.JsLifeCycleListener {
    private final static String LOG_TAG = ThemeFragmentWeb.class.getSimpleName();
    public final static String JS_INTERFACE = "IThemePresenter";
    private ExtendedWebView webView;
    private WebViewClient webViewClient;
    private WebChromeClient chromeClient;
    private ThemeJsInterface jsInterface;

    @Override
    public void scrollToAnchor(String anchor) {
        webView.evalJs("scrollToElement(\"" + anchor + "\")");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        jsInterface = new ThemeJsInterface(presenter);
        messagePanel.setHeightChangeListener(newHeight -> {
            webView.setPaddingBottom(newHeight);
        });
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        attachWebView(webView);
        webView.setJsLifeCycleListener(this);
        refreshLayout.addView(webView);
        refreshLayoutLongTrigger(refreshLayout);
        webView.addJavascriptInterface(this, "IThemeView");
        webView.addJavascriptInterface(jsInterface, JS_INTERFACE);
        registerForContextMenu(webView);
        fab.setOnClickListener(v -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                webView.pageDown(true);
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                webView.pageUp(true);
            }
        });
        webView.setOnDirectionListener(direction -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                fab.setImageDrawable(App.getVecDrawable(fab.getContext(), R.drawable.ic_arrow_down));
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                fab.setImageDrawable(App.getVecDrawable(fab.getContext(), R.drawable.ic_arrow_up));
            }
        });
        //Кастомизация менюхи при выделении текста
        webView.setActionModeListener((actionMode, callback, type) -> {
            Menu menu = actionMode.getMenu();
            ArrayList<MenuItem> items = new ArrayList<>();
            for (int i = 0; i < menu.size(); i++) {
                items.add(menu.getItem(i));
            }
            menu.clear();

            menu.add(R.string.copy)
                    .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_content_copy))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("copyText()");
                        actionMode.finish();
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (presenter.canQuote())
                menu.add(R.string.quote)
                        .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_quote_post))
                        .setOnMenuItemClickListener(item -> {
                            webView.evalJs("selectionToQuote()");
                            actionMode.finish();
                            return true;
                        })
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(R.string.all_text)
                    .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_select_all))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("selectAllPostText()");
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(R.string.share)
                    .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_share))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("shareText()");
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            for (MenuItem item : items) {
                if (item.getIntent() != null) {
                    menu.add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle())
                            .setIntent(item.getIntent())
                            .setNumericShortcut(item.getNumericShortcut())
                            .setAlphabeticShortcut(item.getAlphabeticShortcut());
                }
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void findNext(boolean next) {
        webView.findNext(next);
    }

    @Override
    public void findText(@NotNull String text) {
        webView.findAllAsync(text);
    }

    @Override
    public void updateView(@NotNull ThemePage page) {
        super.updateView(page);
        if (webViewClient == null) {
            webViewClient = new ThemeFragmentWeb.ThemeWebViewClient();
            webView.setWebViewClient(webViewClient);
        }
        if (chromeClient == null) {
            chromeClient = new ThemeFragmentWeb.ThemeChromeClient();
            webView.setWebChromeClient(chromeClient);
        }
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", page.getHtml(), "text/html", "utf-8", null);
        webView.updatePaddingBottom();
    }

    @Override
    public void updateShowAvatarState(boolean isShow) {
        webView.evalJs("updateShowAvatarState(" + isShow + ")");
    }

    @Override
    public void updateTypeAvatarState(boolean isCircle) {
        webView.evalJs("updateTypeAvatarState(" + isCircle + ")");
    }

    @Override
    public void setFontSize(int size) {
        webView.setRelativeFontSize(size);
    }

    @Override
    public void updateHistoryLastHtml() {
        //Log.d(LOG_TAG, "updateHistoryLastHtml " + currentPage);
        webView.evalJs("IThemeView.callbackUpdateHistoryHtml('<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
        Log.d(LOG_TAG, "save scrollY " + webView.getScrollY());
        webView.evalJs("console.log('JAVASCRIPT save scrollY '+window.scrollY)");
    }

    @JavascriptInterface
    public void callbackUpdateHistoryHtml(String value) {
        runInUiThread(() -> presenter.updateHistoryLastHtml(value, webView.getScrollY()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface("IThemeView");
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.setJsLifeCycleListener(null);
        webView.endWork();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
        Log.d(LOG_TAG, "DOMContentLoaded");
        actions.add("setLoadAction(" + presenter.getLoadAction() + ");");
        //Log.e("WebConsole", "" + currentPage.getScrollY() + " : " + App.get().getDensity() + " : " + ((int) (currentPage.getScrollY() / App.get().getDensity())));
        actions.add("setLoadScrollY(" + ((int) (presenter.getPageScrollY() / App.get().getDensity())) + ");");
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
        presenter.setLoadAction(ThemePresenter.ActionState.NORMAL);
        actions.add("setLoadAction(" + ThemePresenter.ActionState.NORMAL + ");");
    }

    @Override
    public void deletePostUi(@NotNull IBaseForumPost post) {
        webView.evalJs("onDeletePostClick(" + post.getId() + ");");
    }

    @Override
    public void openAnchorDialog(@NotNull IBaseForumPost post, @NotNull String anchorName) {
        dialogsHelper.openAnchorDialog(presenter, post, anchorName);
    }

    @Override
    public void openSpoilerLinkDialog(@NotNull IBaseForumPost post, @NotNull String spoilNumber) {
        dialogsHelper.openSpoilerLinkDialog(presenter, post, spoilNumber);
    }

    private class ThemeWebViewClient extends CustomWebViewClient {
        private final Pattern p = Pattern.compile("\\.(jpg|png|gif|bmp)");
        private Matcher m = p.matcher("");

        @Override
        public boolean handleUri(Uri uri) {
            presenter.handleNewUrl(uri);
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (presenter.getLoadAction() == ThemePresenter.ActionState.NORMAL) {
                if (!url.contains("forum/uploads") && !url.contains("android_asset") && !url.contains("style_images") && m.reset(url).find()) {
                    webView.evalJs("onProgressChanged()");
                }
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            /*//TODO сделать привязку к событиям js, вместо этого говнища
            updateHistoryLastHtml();*/
        }
    }

    private class ThemeChromeClient extends CustomWebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (presenter.getLoadAction() == ThemePresenter.ActionState.NORMAL) {
                webView.evalJs("onProgressChanged()");
            }
        }
    }

}
