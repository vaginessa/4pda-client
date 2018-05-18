package forpdateam.ru.forpda.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import forpdateam.ru.forpda.ui.TabManagerProvider
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
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment

class Router(
        private val context: Context,
        private var tabManagerProvider: TabManagerProvider
) : IRouter {

    private fun getTabManager() = tabManagerProvider.getTabManager()

    override fun navigateTo(screen: Screen) {
        val args = Bundle().apply {
            screen.screenTitle?.let {
                putString(TabFragment.ARG_TITLE, it)
            }
            screen.screenSubTitle?.let {
                putString(TabFragment.ARG_SUBTITLE, it)
            }
        }
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
            is Screen.Auth -> getTabManager().add(AuthFragment::class.java)
            is Screen.DevDbDevices -> {
                getTabManager().add(DevicesFragment::class.java, args.apply {
                    putString(DevicesFragment.ARG_CATEGORY_ID, screen.categoryId)
                    putString(DevicesFragment.ARG_BRAND_ID, screen.brandId)
                })
            }
            is Screen.DevDbBrands -> getTabManager().add(BrandsFragment::class.java)
            is Screen.DevDbDevice -> {
                getTabManager().add(DeviceFragment::class.java, args.apply {
                    putString(DeviceFragment.ARG_DEVICE_ID, screen.deviceId)
                })
            }
            is Screen.DevDbSearch -> getTabManager().add(DevDbSearchFragment::class.java)
            is Screen.EditPost -> {
                if (screen.editPostForm == null) {
                    getTabManager().add(EditPostFragment.newInstance(
                            screen.postId,
                            screen.topicId,
                            screen.forumId,
                            screen.st,
                            screen.themeName
                    ))
                } else {
                    getTabManager().add(EditPostFragment.newInstance(
                            screen.editPostForm,
                            screen.themeName
                    ))
                }
                getTabManager().add(EditPostFragment::class.java)
            }
            is Screen.Favorites -> getTabManager().add(FavoritesFragment::class.java)
            is Screen.Forum -> {
                getTabManager().add(ForumFragment::class.java, args.apply {
                    putInt(ForumFragment.ARG_FORUM_ID, screen.forumId)
                })
            }
            is Screen.History -> getTabManager().add(HistoryFragment::class.java)
            is Screen.Mentions -> getTabManager().add(MentionsFragment::class.java)
            is Screen.ArticleList -> getTabManager().add(NewsMainFragment::class.java)
            is Screen.ArticleDetail -> {
                getTabManager().add(NewsDetailsFragment::class.java, args.apply {
                    putInt(NewsDetailsFragment.ARG_NEWS_ID, screen.articleId)
                    putInt(NewsDetailsFragment.ARG_NEWS_COMMENT_ID, screen.commentId)
                    putString(NewsDetailsFragment.ARG_NEWS_URL, screen.articleUrl)
                    putString(NewsDetailsFragment.ARG_NEWS_TITLE, screen.screenTitle)
                    putString(NewsDetailsFragment.ARG_NEWS_AUTHOR_NICK, screen.articleAuthorNick)
                    putString(NewsDetailsFragment.ARG_NEWS_DATE, screen.articleDate)
                    putString(NewsDetailsFragment.ARG_NEWS_IMAGE, screen.articleImageUrl)
                    putInt(NewsDetailsFragment.ARG_NEWS_COMMENTS_COUNT, screen.articleCommentsCount)
                })
            }
            is Screen.Notes -> getTabManager().add(NotesFragment::class.java)
            is Screen.Announce -> {
                getTabManager().add(AnnounceFragment::class.java, args.apply {
                    putInt(AnnounceFragment.ARG_ANNOUNCE_ID, screen.announceId)
                    putInt(AnnounceFragment.ARG_FORUM_ID, screen.forumId)
                })
            }
            is Screen.ForumRules -> getTabManager().add(ForumRulesFragment::class.java)
            is Screen.GoogleCaptcha -> getTabManager().add(GoogleCaptchaFragment::class.java)
            is Screen.Profile -> {
                getTabManager().add(ProfileFragment::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.profileUrl)
                })
            }
            is Screen.QmsContacts -> getTabManager().add(QmsContactsFragment::class.java)
            is Screen.QmsBlackList -> getTabManager().add(QmsBlackListFragment::class.java)
            is Screen.QmsThemes -> {
                getTabManager().add(QmsThemesFragment::class.java, args.apply {
                    putInt(QmsThemesFragment.USER_ID_ARG, screen.userId)
                    putString(QmsThemesFragment.USER_AVATAR_ARG, screen.avatarUrl)
                })
            }
            is Screen.QmsChat -> {
                getTabManager().add(QmsChatFragment::class.java, args.apply {
                    putInt(QmsChatFragment.THEME_ID_ARG, screen.themeId)
                    putInt(QmsChatFragment.USER_ID_ARG, screen.userId)
                    putString(QmsChatFragment.USER_NICK_ARG, screen.userNick)
                    putString(QmsChatFragment.USER_AVATAR_ARG, screen.avatarUrl)
                    putString(QmsChatFragment.THEME_TITLE_ARG, screen.themeTitle)
                })
            }
            is Screen.Reputation -> {
                getTabManager().add(ReputationFragment::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.reputationUrl)
                })
            }
            is Screen.Search -> {
                getTabManager().add(SearchFragment::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.searchUrl)
                })
            }
            is Screen.Theme -> {
                getTabManager().add(ThemeFragmentWeb::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.themeUrl)
                })
            }
            is Screen.Topics -> {
                getTabManager().add(TopicsFragment::class.java, args.apply {
                    putInt(TopicsFragment.TOPICS_ID_ARG, screen.forumId)
                })
            }
            else -> {
                throw Exception("What is screen: \"$screen\" bro? I don't know this screen. Look at this beautiful exception ))0)")
            }
        }

    }
}