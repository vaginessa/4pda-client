package forpdateam.ru.forpda.base

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * Created by isanechek on 7/6/17.
 */
abstract class BaseLifecycleFragment<T : ViewModel> : Fragment(), LifecycleRegistryOwner {
    abstract val viewModelClass: Class<T>

    protected lateinit var viewModel: T

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = registry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(viewModelClass)
    }
}