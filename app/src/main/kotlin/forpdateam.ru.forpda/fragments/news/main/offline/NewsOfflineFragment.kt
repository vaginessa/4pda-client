package forpdateam.ru.forpda.fragments.news.main.offline

import android.arch.lifecycle.LifecycleFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import forpdateam.ru.forpda.R

/**
 * Created by isanechek on 7/22/17.
 */
class NewsOfflineFragment : LifecycleFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.news_offline_fragment_layout, container, false)

    companion object {
        fun createInstance() : NewsOfflineFragment = NewsOfflineFragment()
    }
}