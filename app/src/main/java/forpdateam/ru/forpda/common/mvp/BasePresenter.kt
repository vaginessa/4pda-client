package forpdateam.ru.forpda.common.mvp

import android.os.Handler
import android.os.Looper
import android.view.View
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import forpdateam.ru.forpda.common.ErrorHandler
import forpdateam.ru.forpda.ui.TabManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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
