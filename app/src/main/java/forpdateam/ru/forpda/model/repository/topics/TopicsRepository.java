package forpdateam.ru.forpda.model.repository.topics;

import forpdateam.ru.forpda.api.topcis.Topics;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import forpdateam.ru.forpda.model.SchedulersProvider;
import io.reactivex.Observable;

/**
 * Created by radiationx on 03.01.18.
 */

public class TopicsRepository {

    private SchedulersProvider schedulers;
    private Topics topicsApi;

    public TopicsRepository(SchedulersProvider schedulers, Topics topicsApi) {
        this.schedulers = schedulers;
        this.topicsApi = topicsApi;
    }


    public Observable<TopicsData> getTopics(int id, int st) {
        return Observable
                .fromCallable(() -> topicsApi.getTopics(id, st))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

}
