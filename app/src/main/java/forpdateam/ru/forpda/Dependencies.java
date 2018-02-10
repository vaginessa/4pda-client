package forpdateam.ru.forpda;

import android.content.Context;

import forpdateam.ru.forpda.api.auth.Auth;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.forum.Forum;
import forpdateam.ru.forpda.api.mentions.Mentions;
import forpdateam.ru.forpda.api.profile.Profile;
import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.topcis.Topics;
import forpdateam.ru.forpda.model.NetworkStateProvider;
import forpdateam.ru.forpda.model.repository.auth.AuthRepository;
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository;
import forpdateam.ru.forpda.model.repository.forum.ForumRepository;
import forpdateam.ru.forpda.model.repository.history.HistoryRepository;
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository;
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository;
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository;
import forpdateam.ru.forpda.model.repository.topics.TopicsRepository;
import forpdateam.ru.forpda.model.system.AppNetworkState;
import forpdateam.ru.forpda.model.system.AppSchedulers;
import forpdateam.ru.forpda.model.SchedulersProvider;

/**
 * Created by radiationx on 01.01.18.
 */

public class Dependencies {
    private Context context;
    public NetworkStateProvider networkState;

    Dependencies(Context context) {
        this.context = context;
        networkState = new AppNetworkState(context);
    }

    public SchedulersProvider schedulers = new AppSchedulers();


    public Favorites favoritesApi = new Favorites();
    public Mentions mentionsApi = new Mentions();
    public Auth authApi = new Auth();
    public Profile profileApi = new Profile();
    public Reputation reputationApi = new Reputation();
    public Forum forumApi = new Forum();
    public Topics topicsApi = new Topics();

    public FavoritesRepository favoritesRepository = new FavoritesRepository(schedulers, favoritesApi);
    public HistoryRepository historyRepository = new HistoryRepository(schedulers);
    public MentionsRepository mentionsRepository = new MentionsRepository(schedulers, mentionsApi);
    public AuthRepository authRepository = new AuthRepository(schedulers, authApi);
    public ProfileRepository profileRepository = new ProfileRepository(schedulers, profileApi);
    public ReputationRepository reputationRepository = new ReputationRepository(schedulers, reputationApi);
    public ForumRepository forumRepository = new ForumRepository(schedulers, forumApi);
    public TopicsRepository topicsRepository = new TopicsRepository(schedulers, topicsApi);
}
