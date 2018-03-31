package forpdateam.ru.forpda.ui.fragments.qms;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser;
import forpdateam.ru.forpda.entity.remote.qms.QmsContact;
import forpdateam.ru.forpda.entity.remote.qms.QmsContact;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher;
import forpdateam.ru.forpda.presentation.qms.blacklist.QmsBlackListPresenter;
import forpdateam.ru.forpda.presentation.qms.blacklist.QmsBlackListView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;

/**
 * Created by radiationx on 22.03.17.
 */

public class QmsBlackListFragment extends RecyclerFragment implements QmsContactsAdapter.OnItemClickListener<QmsContact>, QmsBlackListView {
    private AppCompatAutoCompleteTextView nickField;
    private QmsContactsAdapter adapter;
    private DynamicDialogMenu<QmsBlackListFragment, QmsContact> dialogMenu = new DynamicDialogMenu<>();

    @InjectPresenter
    QmsBlackListPresenter presenter;

    @ProvidePresenter
    QmsBlackListPresenter providePresenter() {
        return new QmsBlackListPresenter(App.get().Di().getQmsRepository());
    }

    public QmsBlackListFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_blacklist));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_qms_black_list);
        viewStub.inflate();
        nickField = (AppCompatAutoCompleteTextView) findViewById(R.id.qms_black_list_nick_field);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nickField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.searchUser(s.toString());
            }
        });

        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dialogMenu.addItem(getString(R.string.profile), (context, data) -> presenter.openProfile(data));
        dialogMenu.addItem(getString(R.string.dialogs), (context, data) -> presenter.openDialogs(data));
        dialogMenu.addItem(getString(R.string.delete), (context, data) -> presenter.unBlockUser(data.getId()));

        adapter = new QmsContactsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.add)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_add))
                .setOnMenuItemClickListener(item -> {
                    String nick = "";
                    if (nickField.getText() != null)
                        nick = nickField.getText().toString();
                    presenter.blockUser(nick);
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void showContacts(@NotNull List<? extends QmsContact> items) {
        setRefreshing(false);
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_contacts)
                        .setTitle(R.string.funny_blacklist_nodata_title)
                        .setDesc(R.string.funny_blacklist_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }
        recyclerView.scrollToPosition(0);
        adapter.addAll(items);
    }

    @Override
    public void clearNickField() {
        nickField.setText("");
    }

    @Override
    public void showFoundUsers(@NotNull List<? extends ForumUser> items) {
        List<String> nicks = new ArrayList<>();
        for (ForumUser user : items) {
            nicks.add(user.getNick());
        }
        nickField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, nicks));
    }

    private void someClick(QmsContact contact) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), QmsBlackListFragment.this, contact);
    }

    @Override
    public void onItemClick(QmsContact item) {
        someClick(item);
    }

    @Override
    public boolean onItemLongClick(QmsContact item) {
        someClick(item);
        return false;
    }
}
