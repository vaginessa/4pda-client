package forpdateam.ru.forpda.presentation.theme

import com.arellomobile.mvp.InjectViewState

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.presentation.topics.TopicsView

/**
 * Created by radiationx on 15.03.18.
 */
@InjectViewState
class ThemePresenter(
        private val themeRepository: ThemeRepository
) : BasePresenter<ThemeView>()