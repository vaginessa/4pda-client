package forpdateam.ru.forpda.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.ui.TabManager
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.activities.SettingsActivity
import forpdateam.ru.forpda.ui.activities.WebVewNotFoundActivity
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.activities.updatechecker.UpdateCheckerActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.auth.AuthFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brands.BrandsFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.DeviceFragment
import forpdateam.ru.forpda.ui.fragments.devdb.search.DevDbSearchFragment
import forpdateam.ru.forpda.ui.fragments.editpost.EditPostFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.fragments.forum.ForumFragment
import forpdateam.ru.forpda.ui.fragments.history.HistoryFragment
import forpdateam.ru.forpda.ui.fragments.mentions.MentionsFragment
import forpdateam.ru.forpda.ui.fragments.news.details.NewsDetailsFragment
import forpdateam.ru.forpda.ui.fragments.news.main.NewsMainFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesFragment
import forpdateam.ru.forpda.ui.fragments.other.AnnounceFragment
import forpdateam.ru.forpda.ui.fragments.other.ForumRulesFragment
import forpdateam.ru.forpda.ui.fragments.other.GoogleCaptchaFragment
import forpdateam.ru.forpda.ui.fragments.profile.ProfileFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsBlackListFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsContactsFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsThemesFragment
import forpdateam.ru.forpda.ui.fragments.qms.chat.QmsChatFragment
import forpdateam.ru.forpda.ui.fragments.reputation.ReputationFragment
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment
import forpdateam.ru.forpda.ui.fragments.settings.NotificationsSettingsFragment
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment

class Router(
        private val context: Context,
        private var tabManager: TabManager
) : IRouter {

    override fun navigateTo(screen: Screen) {
        when (screen) {
            is Screen.Main -> {
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.ARG_CHECK_WEBVIEW, screen.checkWebView)
                }
                context.startActivity(intent)
            }
            is Screen.UpdateChecker -> {
                val intent = Intent(context, UpdateCheckerActivity::class.java).apply {
                    putExtra(UpdateCheckerActivity.ARG_JSON_SOURCE, screen.jsonSource)
                }
                context.startActivity(intent)
            }
            is Screen.WebViewNotFound -> {
                context.startActivity(Intent(context, WebVewNotFoundActivity::class.java))
            }
            is Screen.ImageViewer -> {
                ImageViewerActivity.startActivity(context, ArrayList(screen.urls), screen.selected)
            }
            is Screen.Settings -> {
                val intent = Intent(context, SettingsActivity::class.java)
                intent.putExtra(SettingsActivity.ARG_NEW_PREFERENCE_SCREEN, screen.fragment)
                context.startActivity(intent)
            }
            is Screen.Auth -> tabManager.add(AuthFragment::class.java)
            is Screen.DevDbDevices -> {
                tabManager.add(DevicesFragment::class.java, Bundle().apply {
                    putString(DevicesFragment.ARG_CATEGORY_ID, screen.categoryId)
                    putString(DevicesFragment.ARG_BRAND_ID, screen.brandId)
                })
            }
            is Screen.DevDbBrands -> tabManager.add(BrandsFragment::class.java)
            is Screen.DevDbDevice -> {
                tabManager.add(DeviceFragment::class.java, Bundle().apply {
                    putString(DeviceFragment.ARG_DEVICE_ID, screen.deviceId)
                })
            }
            is Screen.DevDbSearch -> tabManager.add(DevDbSearchFragment::class.java)
            is Screen.EditPost -> tabManager.add(EditPostFragment::class.java)
            is Screen.Favorites -> tabManager.add(FavoritesFragment::class.java)
            is Screen.Forum -> tabManager.add(ForumFragment::class.java)
            is Screen.History -> tabManager.add(HistoryFragment::class.java)
            is Screen.Mentions -> tabManager.add(MentionsFragment::class.java)
            is Screen.ArticleList -> tabManager.add(NewsMainFragment::class.java)
            is Screen.ArticleDetail -> {
                tabManager.add(NewsDetailsFragment::class.java, Bundle().apply {
                    putInt(NewsDetailsFragment.ARG_NEWS_ID, screen.articleId)
                    putInt(NewsDetailsFragment.ARG_NEWS_COMMENT_ID, screen.commentId)
                    putString(NewsDetailsFragment.ARG_NEWS_URL, screen.articleUrl)
                })
            }
            is Screen.Notes -> tabManager.add(NotesFragment::class.java)
            is Screen.Announce -> {
                tabManager.add(AnnounceFragment::class.java, Bundle().apply {
                    putInt(AnnounceFragment.ARG_ANNOUNCE_ID, screen.announceId)
                    putInt(AnnounceFragment.ARG_FORUM_ID, screen.forumId)
                })
            }
            is Screen.ForumRules -> tabManager.add(ForumRulesFragment::class.java)
            is Screen.GoogleCaptcha -> tabManager.add(GoogleCaptchaFragment::class.java)
            is Screen.Profile -> {
                tabManager.add(ProfileFragment::class.java, Bundle().apply {
                    putString(TabFragment.ARG_TAB, screen.profileUrl)
                })
            }
            is Screen.QmsContacts -> tabManager.add(QmsContactsFragment::class.java)
            is Screen.QmsBlackList -> tabManager.add(QmsBlackListFragment::class.java)
            is Screen.QmsThemes -> {
                tabManager.add(QmsThemesFragment::class.java, Bundle().apply {
                    putInt(QmsThemesFragment.USER_ID_ARG, screen.userId)
                })
            }
            is Screen.QmsChat -> {
                tabManager.add(QmsChatFragment::class.java, Bundle().apply {
                    putInt(QmsChatFragment.THEME_ID_ARG, screen.themeId)
                    putInt(QmsChatFragment.USER_ID_ARG, screen.userId)
                })
            }
            is Screen.Reputation -> {
                tabManager.add(ReputationFragment::class.java, Bundle().apply {
                    putString(TabFragment.ARG_TAB, screen.reputationUrl)
                })
            }
            is Screen.Search -> {
                tabManager.add(SearchFragment::class.java, Bundle().apply {
                    putString(TabFragment.ARG_TAB, screen.searchUrl)
                })
            }
            is Screen.Theme -> {
                tabManager.add(ThemeFragmentWeb::class.java, Bundle().apply {
                    putString(TabFragment.ARG_TAB, screen.themeUrl)
                })
            }
            is Screen.Topics -> {
                tabManager.add(TopicsFragment::class.java, Bundle().apply {
                    putInt(TopicsFragment.TOPICS_ID_ARG, screen.forumId)
                })
            }
            else -> {
                throw Exception("What is screen: \"$screen\" bro? I don't know this screen. Look at this beautiful exception ))0)")
            }
        }

    }
}