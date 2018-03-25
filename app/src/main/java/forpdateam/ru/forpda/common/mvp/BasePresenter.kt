package forpdateam.ru.forpda.common.mvp

import android.os.Handler
import android.os.Looper
import android.view.View
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import forpdateam.ru.forpda.common.ErrorHandler
import forpdateam.ru.forpda.ui.TabManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.acra.ACRA

/**
 * Created by radiationx on 05.11.17.
 */

open class BasePresenter<V : MvpView> : MvpPresenter<V>() {
    private var compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    fun Disposable.addToDisposable() {
        compositeDisposable.add(this)
    }

    fun <T> subscribe(observable: Observable<T>, onNext: Consumer<T>, onErrorReturn: T) {
        subscribe(observable, onNext, onErrorReturn, null)
    }

    fun <T> subscribe(observable: Observable<T>, onNext: Consumer<T>, onErrorReturn: T, onErrorAction: View.OnClickListener?) {
        observable
                .onErrorReturn { throwable ->
                    handleErrorRx(throwable, onErrorAction)
                    onErrorReturn
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, Consumer<Throwable> {
                    handleErrorRx(it, onErrorAction)
                })
                .addToDisposable()

    }

    @JvmOverloads
    protected fun handleErrorRx(throwable: Throwable, listener: View.OnClickListener? = null) {
        Handler(Looper.getMainLooper()).post {
            val tabFragment = TabManager.get().active
            if (tabFragment == null) {
                throwable.printStackTrace()
                ACRA.getErrorReporter().handleException(throwable)
            } else {
                ErrorHandler.handle(tabFragment, throwable, listener)
            }
        }
    }
}
