package forpdateam.ru.forpda.ui.fragments.forum;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.apirx.RxApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 07.07.17.
 */

public class ForumHelper {
    public static void markRead(@NonNull Consumer<Object> onNext, int id) {
        RxApi.Forum().markRead(id).onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    public static void markAllRead(@NonNull Consumer<Object> onNext) {
        RxApi.Forum().markAllRead().onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }
}
