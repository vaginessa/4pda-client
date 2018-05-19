package forpdateam.ru.forpda.presentation

interface IRouter {
    fun newScreenChain(screen: Screen)
    fun navigateTo(screen: Screen)
    fun backTo(screen: Screen)
    fun replaceScreen(screen: Screen)
    fun newRootScreen(screen: Screen)
}