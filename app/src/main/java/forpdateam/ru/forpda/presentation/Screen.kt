package forpdateam.ru.forpda.presentation

sealed class Screen {
    companion object {
        const val ARG_TITLE = "arg_title"
        const val ARG_SUBTITLE = "arg_subtitle"
        private val NO_ID = -1
    }

    var title: String? = null
    var subTitle: String? = null

    /* Activities */

    class Main : Screen(){
        var checkWebView = true
    }
    class WebViewNotFound : Screen()
    class UpdateChecker : Screen() {
        var jsonSource: String? = null
    }

    class ImageViewer : Screen() {
        var urls: MutableList<String> = mutableListOf()
        var selected: Int = NO_ID
    }

    class Settings : Screen() {
        var fragment: String? = null
    }

    /* Fragments */
    class Auth : Screen()

    class DevDbDevices : Screen() {
        var brandId: String? = null
        var categoryId: String? = null
    }

    class DevDbBrands : Screen() {
        var categoryId: String? = null
    }

    class DevDbDevice : Screen() {
        var deviceId: String? = null
    }

    class DevDbSearch : Screen()

    class EditPost : Screen()

    class Favorites : Screen()

    class Forum : Screen()

    class History : Screen()

    class Mentions : Screen()

    class ArticleList : Screen()
    class ArticleDetail : Screen() {
        var articleId: Int = NO_ID
        var commentId: Int = NO_ID
        var articleUrl: String? = null
    }

    class Notes : Screen()

    class Announce : Screen() {
        var forumId: Int = NO_ID
        var announceId: Int = NO_ID
    }

    class ForumRules : Screen()
    class GoogleCaptcha : Screen()

    class Profile : Screen() {
        var profileUrl: String? = null
    }

    class QmsContacts : Screen()
    class QmsBlackList : Screen()
    class QmsThemes : Screen() {
        var userId: Int = NO_ID
    }

    class QmsChat : Screen() {
        var userId: Int = NO_ID
        var themeId: Int = NO_ID
    }

    class Reputation : Screen() {
        var reputationUrl: String? = null
    }

    class Search : Screen() {
        var searchUrl: String? = null
    }

    class Theme : Screen() {
        var themeUrl: String? = null
    }

    class Topics : Screen() {
        var forumId: Int = NO_ID
    }

}