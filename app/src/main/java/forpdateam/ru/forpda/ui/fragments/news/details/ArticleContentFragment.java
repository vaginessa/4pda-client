package forpdateam.ru.forpda.ui.fragments.news.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import forpdateam.ru.forpda.common.webview.CustomWebChromeClient;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.entity.remote.news.DetailsPage;
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor;
import forpdateam.ru.forpda.presentation.articles.detail.content.ArticleContentPresenter;
import forpdateam.ru.forpda.presentation.articles.detail.content.ArticleContentView;
import forpdateam.ru.forpda.ui.activities.MainActivity;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleContentFragment extends MvpAppCompatFragment implements ArticleContentView {
    public final static String JS_INTERFACE = "INews";
    private ExtendedWebView webView;
    private ArticleInteractor interactor;

    @InjectPresenter
    ArticleContentPresenter presenter;

    @ProvidePresenter
    ArticleContentPresenter providePresenter() {
        return new ArticleContentPresenter(interactor);
    }

    public ArticleContentFragment setInteractor(ArticleInteractor interactor) {
        this.interactor = interactor;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        webView = ((MainActivity) getActivity()).getWebViewsProvider().pull(getContext());
        registerForContextMenu(webView);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
        webView.addJavascriptInterface(this, JS_INTERFACE);
        return webView;
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
    }

    @Override
    public void showData(@NotNull DetailsPage article) {
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", article.getHtml(), "text/html", "utf-8", null);
    }

    @JavascriptInterface
    public void toComments() {
        if (getContext() == null)
            return;
        webView.runInUiThread(() -> {
            ((NewsDetailsFragment) getParentFragment()).getFragmentsPager().setCurrentItem(1);
        });
    }

    @JavascriptInterface
    public void sendPoll(String id, String answer, String from) {
        if (getContext() == null)
            return;
        webView.runInUiThread(() -> {
            int pollId = Integer.parseInt(id);
            String[] answers = answer.split(",");
            int answersId[] = new int[answers.length];
            for (int i = 0; i < answers.length; i++) {
                answersId[i] = Integer.parseInt(answers[i]);
            }
            presenter.sendPoll(from, pollId, answersId);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.endWork();
            ((MainActivity) getActivity()).getWebViewsProvider().push(webView);
        }
    }
}
