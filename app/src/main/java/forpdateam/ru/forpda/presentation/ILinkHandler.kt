package forpdateam.ru.forpda.presentation

/**
 * Created by radiationx on 03.02.18.
 */
interface ILinkHandler {
    fun handle(inputUrl: String?, router: Router?, doNavigate: Boolean = true): Boolean
    fun findScreen(url: String): String?
}