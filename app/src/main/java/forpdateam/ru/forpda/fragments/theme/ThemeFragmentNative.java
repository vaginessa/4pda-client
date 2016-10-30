package forpdateam.ru.forpda.fragments.theme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.fragments.theme.adapters.ThemeAdapter;
import forpdateam.ru.forpda.utils.ErrorHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 05.08.16.
 */
public class ThemeFragmentNative extends ThemeFragment {
    private RecyclerView recyclerView;
    private ThemeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.fragment_theme_test, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        recyclerView = (RecyclerView) findViewById(R.id.theme);
        viewsReady();
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        //llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        return view;
    }

    @Override
    public void loadData() {
        getCompositeDisposable().add(Api.Theme().getPage(getTabUrl())
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ThemePage();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindUi, throwable -> {
                    ErrorHandler.handle(this, throwable, null);
                }));
    }

    String template =
            "%s : %s : %s : %s";

    private void bindUi(ThemePage page) {

        /*Log.d("kek", "bindui");

        setTitle(page.getTitle());
        setSubtitle(page.getDesc());
        String temp = "";
        temp += page.getCurrentPage() + " : " + page.isHaveFirstPage() + " : " + page.isHaveLastPage() + " : " + page.getAllPagesCount() + " : " + page.getPostsOnPageCount() + "\n\n\n";
        String postFix = " : ";
        for (ThemePost post : page.getPosts()) {
            temp += String.format(template, post.getId(), post.getDate(), post.getNumber(), post.getAvatar());
            temp += "\n\n";
            temp += post.getId() + postFix;
            temp += post.getDate() + postFix;
            temp += post.getNumber() + postFix;
            temp += post.getAvatar() + postFix;
            temp += post.getNick() + postFix;
            temp += post.getGroupColor() + postFix;
            temp += post.getGroup() + postFix;
            temp += post.getUserId() + postFix;
            temp += post.getReputation() + postFix;
            temp += post.isCurator() + postFix;
            temp += post.isOnline() + postFix;
            temp += post.canMinusRep() + postFix;
            temp += post.canPlusRep() + postFix;
            temp += post.canReport() + postFix;
            temp += post.canEdit() + postFix;
            temp += post.canDelete() + postFix;
            temp += post.canQuote() + postFix;
            temp += "\n\n";
        }
        text.setText(temp);
        Log.d("kek", "time " + (new Date().getTime() - date.getTime()));*/
        Date date = new Date();
        setTitle(page.getTitle());
        setSubtitle(page.getDesc());
        adapter = new ThemeAdapter(page.getPosts(), getContext());
        recyclerView.setAdapter(adapter);
        Log.d("kek", "theme UI CREATE time " + (new Date().getTime() - date.getTime()));
        //recyclerView.scrollToPosition(adapter.getItemCount()-1);
    }
}