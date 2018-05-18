package forpdateam.ru.forpda.ui.fragments.forum;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.jetbrains.annotations.NotNull;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree;
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi;
import forpdateam.ru.forpda.presentation.forum.ForumPresenter;
import forpdateam.ru.forpda.presentation.forum.ForumView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumFragment extends TabFragment implements ForumView {
    public final static String ARG_FORUM_ID = "ARG_FORUM_ID";

    private TreeNode root;
    private AndroidTreeView tView;
    private int forumId = -1;

    private NestedScrollView treeContainer;
    private DynamicDialogMenu<ForumFragment, ForumItemTree> dialogMenu;
    private TreeNode.TreeNodeClickListener nodeClickListener = (node, value) -> {
        ForumItemTree item = (ForumItemTree) value;
        if (item.getForums() == null) {
            Bundle args = new Bundle();
            args.putInt(TopicsFragment.TOPICS_ID_ARG, item.getId());
            TabManager.get().add(TopicsFragment.class, args);
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = (node, value) -> {
        ForumItemTree item = (ForumItemTree) value;
        dialogMenu.disallowAll();
        if (item.getLevel() > 0)
            dialogMenu.allow(0);
        dialogMenu.allow(1);
        if (ClientHelper.getAuthState()) {
            dialogMenu.allow(2);
            dialogMenu.allow(3);
        }
        dialogMenu.allow(4);

        dialogMenu.show(getContext(), ForumFragment.this, item);
        return false;
    };

    @InjectPresenter
    ForumPresenter presenter;

    @ProvidePresenter
    ForumPresenter providePresenter() {
        return new ForumPresenter(
                App.get().Di().getForumRepository(),
                App.get().Di().getFavoritesRepository(),
                App.get().Di().getRouter()
        );
    }

    public ForumFragment() {
        configuration.setUseCache(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_forum));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            forumId = getArguments().getInt(ARG_FORUM_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_forum);
        treeContainer = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListsBackground();

        dialogMenu = new DynamicDialogMenu<>();
        dialogMenu.addItem(getString(R.string.open_forum), (context, data) -> presenter.navigateToForum(data));
        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.mark_read), (context, data) -> openMarkReadDialog(data));
        dialogMenu.addItem(getString(R.string.add_to_favorites), (context, data) -> openAddToFavoriteDialog(data.getId()));
        dialogMenu.addItem(getString(R.string.fragment_title_search), (context, data) -> presenter.navigateToSearch(data));
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.forum_refresh)
                .setOnMenuItemClickListener(item -> {
                    presenter.loadForums();
                    return false;
                });
        menu.add(R.string.mark_all_read)
                .setOnMenuItemClickListener(item -> {
                    openMarkAllReadDialog();
                    return false;
                });
    }

    @Override
    public void showForums(@NotNull ForumItemTree forumRoot) {
        tView = new AndroidTreeView(getContext());
        root = TreeNode.root();
        recourse(forumRoot, root);
        tView.setRoot(root);

        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(DefaultForumHolder.class);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);
        treeContainer.removeAllViews();
        treeContainer.addView(tView.getView());

        if (forumId != -1) {
            scrollToForum(forumId);
            forumId = -1;
        }
    }

    public void openAddToFavoriteDialog(int forumId) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES, (dialog1, which1) -> {
                    presenter.addToFavorite(forumId, FavoritesApi.SUB_TYPES[which1]);
                })
                .show();
    }

    public void openMarkReadDialog(ForumItemTree item) {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.mark_read) + "?")
                .setPositiveButton(R.string.ok, (dialog, which) -> presenter.markRead(item.getId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void openMarkAllReadDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.mark_all_read) + "?")
                .setPositiveButton(R.string.ok, (dialog, which) -> presenter.markAllRead())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onMarkRead() {
        Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkAllRead() {
        Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddToFavorite(boolean result) {
        Toast.makeText(getContext(), result ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
    }

    private void scrollToForum(int id) {
        final TreeNode targetNode = findNodeById(id, root);

        if (targetNode != null) {
            TreeNode upToParent = targetNode;
            while (upToParent.getParent() != null) {
                tView.expandNode(upToParent);
                upToParent = upToParent.getParent();
            }
        }
    }

    private TreeNode findNodeById(int id, TreeNode root) {
        if (root.getValue() != null && ((ForumItemTree) root.getValue()).getId() == id) return root;
        if (root.getChildren() == null && root.getChildren().isEmpty()) return null;
        for (TreeNode item : root.getChildren()) {
            TreeNode node = findNodeById(id, item);
            if (node != null) return node;
        }
        return null;
    }

    private void recourse(ForumItemTree rootForum, TreeNode rootNode) {
        if (rootForum.getForums() == null) return;
        for (ForumItemTree item : rootForum.getForums()) {
            TreeNode child = new TreeNode(item);
            recourse(item, child);
            rootNode.addChild(child);
        }
    }

}
