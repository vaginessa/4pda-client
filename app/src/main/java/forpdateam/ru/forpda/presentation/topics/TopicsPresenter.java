package forpdateam.ru.forpda.presentation.topics;

import android.os.Bundle;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository;
import forpdateam.ru.forpda.model.repository.topics.TopicsRepository;
import forpdateam.ru.forpda.presentation.reputation.ReputationView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.forum.ForumFragment;
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
public class TopicsPresenter extends BasePresenter<TopicsView> {

    private int id = 0;
    private int currentSt = 0;
    private TopicsData currentData;

    private TopicsRepository topicsRepository;

    public TopicsPresenter(TopicsRepository topicsRepository) {
        this.topicsRepository = topicsRepository;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCurrentData(TopicsData data) {
        currentData = data;
    }

    public TopicsData getCurrentData() {
        return currentData;
    }

    public void loadTopics() {
        Disposable disposable
                = topicsRepository.getTopics(id, currentSt)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(repData1 -> getViewState().showTopics(repData1), this::handleErrorRx);

        addToDisposable(disposable);
    }

    public void loadPage(int st) {
        currentSt = st;
        loadTopics();
    }

    public void openForum() {
        Bundle args = new Bundle();
        args.putInt(ForumFragment.ARG_FORUM_ID, id);
        TabManager.get().add(ForumFragment.class, args);
    }

    public void openSearch() {
        String url = "https://4pda.ru/forum/index.php?act=search&source=all&forums%5B%5D=" + id;
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TAB, url);
        TabManager.get().add(SearchFragment.class, args);
    }

    public void onItemClick(TopicItem item) {
        if (item.isAnnounce()) {
            Bundle args = new Bundle();
            args.putString(TabFragment.ARG_TITLE, item.getTitle());
            IntentHandler.handle(item.getAnnounceUrl(), args);
            return;
        }
        if (item.isForum()) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.getId());
            return;
        }
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTitle());
        IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + item.getId() + "&view=getnewpost", args);
    }

    public void onItemLongClick(TopicItem item) {
        getViewState().showItemDialogMenu(item);
    }
}
