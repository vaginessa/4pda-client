package forpdateam.ru.forpda.common.mvp

import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import forpdateam.ru.forpda.App
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

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
        throwable.printStackTrace()
        Toast.makeText(App.getContext(), "HandleErrorRx: ${throwable.message}", Toast.LENGTH_SHORT).show()
    }
}
