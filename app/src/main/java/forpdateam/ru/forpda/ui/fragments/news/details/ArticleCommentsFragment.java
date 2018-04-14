package forpdateam.ru.forpda.ui.fragments.news.details;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher;
import forpdateam.ru.forpda.entity.remote.news.Comment;
import forpdateam.ru.forpda.entity.remote.news.DetailsPage;
import forpdateam.ru.forpda.model.interactors.devdb.ArticleInteractor;
import forpdateam.ru.forpda.presentation.articles.detail.comments.ArticleCommentPresenter;
import forpdateam.ru.forpda.presentation.articles.detail.comments.ArticleCommentView;
import forpdateam.ru.forpda.ui.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleCommentsFragment extends Fragment implements ArticleCommentView, ArticleCommentsAdapter.ClickListener {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private EditText messageField;
    private AppCompatImageButton buttonSend;
    private ProgressBar progressBarSend;
    private RelativeLayout writePanel;
    private ArticleCommentsAdapter adapter;
    private Comment currentReplyComment;
    private ContentController contentController;

    private Observer loginObserver = (observable, o) -> {
        if (o == null) o = false;
        if (ClientHelper.getAuthState()) {
            writePanel.setVisibility(View.VISIBLE);
        } else {
            writePanel.setVisibility(View.GONE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    };

    private ArticleInteractor interactor;

    @InjectPresenter
    ArticleCommentPresenter presenter;

    @ProvidePresenter
    ArticleCommentPresenter provideMentionsPresenter() {
        return new ArticleCommentPresenter(interactor);
    }

    public ArticleCommentsFragment setInteractor(ArticleInteractor interactor) {
        this.interactor = interactor;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.article_comments, container, false);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) view.findViewById(R.id.base_list);
        writePanel = (RelativeLayout) view.findViewById(R.id.comment_write_panel);
        messageField = (EditText) view.findViewById(R.id.message_field);
        FrameLayout sendContainer = (FrameLayout) view.findViewById(R.id.send_container);
        buttonSend = (AppCompatImageButton) view.findViewById(R.id.button_send);
        progressBarSend = (ProgressBar) view.findViewById(R.id.send_progress);
        ViewGroup additionalContent = (ViewGroup) view.findViewById(R.id.additional_content);
        contentController = new ContentController(null, additionalContent, refreshLayout);

        refreshLayout.setProgressBackgroundColorSchemeColor(App.getColorFromAttr(getContext(), R.attr.colorPrimary));
        refreshLayout.setColorSchemeColors(App.getColorFromAttr(getContext(), R.attr.colorAccent));
        refreshLayout.setOnRefreshListener(() -> presenter.updateComments());

        recyclerView.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_lists));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px12, false));
        adapter = new ArticleCommentsAdapter();
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        messageField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    currentReplyComment = null;
                }
                buttonSend.setClickable(s.length() > 0);
            }
        });

        buttonSend.setOnClickListener(v -> sendComment());

        if (ClientHelper.getAuthState()) {
            writePanel.setVisibility(View.VISIBLE);
        } else {
            writePanel.setVisibility(View.GONE);
        }

        ClientHelper.get().addLoginObserver(loginObserver);
        return view;
    }

    private void createFunny(@NotNull List<? extends Comment> comments) {
        if (comments.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_comment)
                        .setTitle(R.string.funny_article_comments_nodata_title);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }
    }

    @Override
    public void onNickClick(Comment comment, int position) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + comment.getUserId());
    }

    @Override
    public void onLikeClick(Comment comment, int position) {
        Comment.Karma karma = comment.getKarma();
        karma.setStatus(Comment.Karma.LIKED);
        karma.setCount(karma.getCount() + 1);
        adapter.notifyItemChanged(position);
        presenter.likeComment(comment.getId());
    }

    @Override
    public void onReplyClick(Comment comment, int position) {
        if (messageField.getText().length() == 0) {
            fillMessageField(comment);
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.comment_reply_warning)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> fillMessageField(comment))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }

    }

    private void fillMessageField(Comment comment) {
        currentReplyComment = comment;
        messageField.setText(currentReplyComment.getUserNick() + ",\n");
        messageField.setSelection(messageField.getText().length());
        messageField.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageField, InputMethodManager.SHOW_IMPLICIT);
    }

    private void sendComment() {
        int commentId = currentReplyComment == null ? 0 : currentReplyComment.getId();
        presenter.replyComment(commentId, messageField.getText().toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ClientHelper.get().removeLoginObserver(loginObserver);
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        refreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public void setSendRefreshing(boolean isRefreshing) {
        progressBarSend.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
        buttonSend.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showComments(@NotNull List<? extends Comment> comments) {
        adapter.addAll(comments);
        createFunny(comments);
    }

    @Override
    public void scrollToComment(int position) {
        recyclerView.scrollToPosition(position);
    }

    @Override
    public void onReplyComment() {
        messageField.setText(null);
        currentReplyComment = null;
    }

}
