package forpdateam.ru.forpda.ui.fragments.news.details;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.news.DetailsPage;
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor;
import forpdateam.ru.forpda.presentation.articles.detail.ArticleDetailPresenter;
import forpdateam.ru.forpda.presentation.articles.detail.ArticleDetailView;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.views.ScrimHelper;

/**
 * Created by isanechek on 8/19/17.
 */

public class NewsDetailsFragment extends TabFragment implements ArticleDetailView {

    public static final String ARG_NEWS_URL = "ARG_NEWS_URL";
    public static final String ARG_NEWS_ID = "ARG_NEWS_ID";
    public static final String ARG_NEWS_COMMENT_ID = "ARG_NEWS_COMMENT_ID";
    public static final String ARG_NEWS_TITLE = "ARG_NEWS_TITLE";
    public static final String ARG_NEWS_AUTHOR_NICK = "ARG_NEWS_AUTHOR_NICK";
    public static final String ARG_NEWS_AUTHOR_ID = "ARG_NEWS_AUTHOR_ID";
    public static final String ARG_NEWS_COMMENTS_COUNT = "ARG_NEWS_COMMENTS_COUNT";
    public static final String ARG_NEWS_DATE = "ARG_NEWS_DATE";
    public static final String ARG_NEWS_IMAGE = "ARG_NEWS_IMAGE";


    private FrameLayout webViewContainer;
    private ViewPager fragmentsPager;
    private ProgressBar progressBar;
    private ProgressBar imageProgressBar;
    private ImageView detailsImage;

    private TextView detailsTitle;
    private TextView detailsNick;
    private TextView detailsCount;
    private TextView detailsDate;

    private ArticleInteractor interactor = new ArticleInteractor(
            new ArticleInteractor.InitData(),
            App.get().Di().getNewsRepository(),
            App.get().Di().getArticleTemplate()
    );

    @InjectPresenter
    ArticleDetailPresenter presenter;

    @ProvidePresenter
    ArticleDetailPresenter providePresenter() {
        return new ArticleDetailPresenter(
                interactor,
                App.get().Di().getRouter(),
                App.get().Di().getLinkHandler()
        );
    }

    public NewsDetailsFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_news));
        configuration.setUseCache(false); // back
        configuration.setAlone(false);
        configuration.setFitSystemWindow(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            interactor.getInitData().setNewsUrl(getArguments().getString(ARG_NEWS_URL));
            interactor.getInitData().setNewsId(getArguments().getInt(ARG_NEWS_ID, 0));
            interactor.getInitData().setCommentId(getArguments().getInt(ARG_NEWS_COMMENT_ID, 0));
            /*presenter.setNewsTitle(getArguments().getString(ARG_NEWS_TITLE));
            presenter.setNewsNick(getArguments().getString(ARG_NEWS_AUTHOR_NICK));
            presenter.setNewsDate(getArguments().getString(ARG_NEWS_DATE));
            presenter.setNewsImageUrl(getArguments().getString(ARG_NEWS_IMAGE));
            presenter.setNewsCount(getArguments().getInt(ARG_NEWS_COMMENTS_COUNT, -1));*/
        }
        if (getChildFragmentManager().getFragments() != null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                transaction.remove(fragment);
            }
            transaction.commit();
            getChildFragmentManager().executePendingTransactions();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_article);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_news_details);
        viewStub.inflate();
        fragmentsPager = (ViewPager) findViewById(R.id.view_pager);
        webViewContainer = (FrameLayout) findViewById(R.id.swipe_refresh_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        detailsImage = (ImageView) findViewById(R.id.article_image);
        detailsTitle = (TextView) findViewById(R.id.article_title);
        detailsNick = (TextView) findViewById(R.id.article_nick);
        detailsCount = (TextView) findViewById(R.id.article_comments_count);
        detailsDate = (TextView) findViewById(R.id.article_date);
        imageProgressBar = (ProgressBar) findViewById(R.id.article_progress_bar);

        detailsImage.setMaxHeight(App.px24 * 10);

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        toolbarLayout.setLayoutParams(params);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ScrimHelper scrimHelper = new ScrimHelper(appBarLayout, toolbarLayout);
        scrimHelper.setScrimListener(scrim1 -> {
            if (scrim1) {
                toolbar.getNavigationIcon().clearColorFilter();
                toolbar.getOverflowIcon().clearColorFilter();
                toolbarTitleView.setVisibility(View.VISIBLE);
            } else {
                toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbarTitleView.setVisibility(View.GONE);
            }
        });


        if (getArguments() != null) {
            String newsTitle = getArguments().getString(ARG_NEWS_TITLE);
            String newsNick = getArguments().getString(ARG_NEWS_AUTHOR_NICK);
            String newsDate = getArguments().getString(ARG_NEWS_DATE);
            String newsImageUrl = getArguments().getString(ARG_NEWS_IMAGE);
            int newsCount = getArguments().getInt(ARG_NEWS_COMMENTS_COUNT, -1);
            if (newsTitle != null) {
                setTitle(newsTitle);
                setTabTitle(String.format(getString(R.string.fragment_tab_title_article), newsTitle));
                detailsTitle.setText(newsTitle);
            }
            if (newsNick != null) {
                detailsNick.setText(newsNick);
            }
            if (newsCount != -1) {
                detailsCount.setText(Integer.toString(newsCount));
            }
            if (newsDate != null) {
                detailsDate.setText(newsDate);
            }
            if (newsImageUrl != null) {
                showArticleImage(newsImageUrl);
            }
        }

        toolbarTitleView.setVisibility(View.GONE);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        detailsNick.setOnClickListener(v -> presenter.openAuthorProfile());
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.copy_link)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.copyLink();
                    return false;
                });
        menu.add(R.string.share)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.shareLink();
                    return false;
                });
        menu.add(R.string.create_note)
                .setOnMenuItemClickListener(menuItem -> {
                    presenter.createNote();
                    return false;
                });
    }

    @Override
    public boolean onBackPressed() {
        if (fragmentsPager.getCurrentItem() == 1) {
            fragmentsPager.setCurrentItem(0);
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        progressBar.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showArticle(@NotNull DetailsPage data) {
        setTitle(data.getTitle());
        setTabTitle(String.format(getString(R.string.fragment_tab_title_article), data.getTitle()));
        detailsTitle.setText(data.getTitle());
        detailsNick.setText(data.getAuthor());
        detailsDate.setText(data.getDate());
        detailsCount.setText(Integer.toString(data.getCommentsCount()));

        if (data.getImgUrl() != null) {
            showArticleImage(data.getImgUrl());
        }

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getChildFragmentManager(), interactor);
        fragmentsPager.setAdapter(pagerAdapter);
        if (data.getCommentId() != 0) {
            appBarLayout.setExpanded(false, true);
            fragmentsPager.setCurrentItem(1, true);
        }
    }

    @Override
    public void showCreateNote(@NotNull String title, @NotNull String url) {
        NotesAddPopup.showAddNoteDialog(getContext(), title, url);
    }

    public ViewPager getFragmentsPager() {
        return fragmentsPager;
    }

    @Override
    public void showArticleImage(@NotNull String imageUrl) {
        ImageLoader.getInstance().displayImage(imageUrl, detailsImage, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
        private ArticleInteractor interactor;
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        public FragmentPagerAdapter(FragmentManager fm, ArticleInteractor interactor) {
            super(fm);
            this.interactor = interactor;

            fragments.add(new ArticleContentFragment().setInteractor(this.interactor));
            titles.add(App.get().getString(R.string.news_page_content));

            fragments.add(new ArticleCommentsFragment().setInteractor(this.interactor));
            titles.add(App.get().getString(R.string.news_page_comments));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


}
