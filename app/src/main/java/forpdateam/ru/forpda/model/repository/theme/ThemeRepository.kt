package forpdateam.ru.forpda.model.repository.theme

import forpdateam.ru.forpda.api.theme.Theme
import forpdateam.ru.forpda.model.SchedulersProvider

/**
 * Created by radiationx on 15.03.18.
 */

class ThemeRepository(
        private val schedulers: SchedulersProvider,
        private val themeApi: Theme
)
