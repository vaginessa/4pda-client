package forpdateam.ru.forpda.ui

import android.os.Bundle
import forpdateam.ru.forpda.presentation.Screen
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

class TabHelper {

    private fun createFragment(tabClass: Class<out TabFragment>, args: Bundle? = null): TabFragment {
        return tabClass.newInstance().apply {
            args?.let { arguments = it }
        }
    }

    fun createTab(screen: Screen): TabFragment {
        val args = Bundle().apply {
            screen.screenTitle?.let {
                putString(TabFragment.ARG_TITLE, it)
            }
            screen.screenSubTitle?.let {
                putString(TabFragment.ARG_SUBTITLE, it)
            }
        }
        return when (screen) {
            is Screen.Auth -> createFragment(AuthFragment::class.java, args)
            is Screen.DevDbDevices -> {
                createFragment(DevicesFragment::class.java, args.apply {
                    putString(DevicesFragment.ARG_CATEGORY_ID, screen.categoryId)
                    putString(DevicesFragment.ARG_BRAND_ID, screen.brandId)
                })
            }
            is Screen.DevDbBrands -> createFragment(BrandsFragment::class.java, args)
            is Screen.DevDbDevice -> {
                createFragment(DeviceFragment::class.java, args.apply {
                    putString(DeviceFragment.ARG_DEVICE_ID, screen.deviceId)
                })
            }
            is Screen.DevDbSearch -> createFragment(DevDbSearchFragment::class.java, args)
            is Screen.EditPost -> {
                if (screen.editPostForm == null) {
                    EditPostFragment.newInstance(
                            screen.postId,
                            screen.topicId,
                            screen.forumId,
                            screen.st,
                            screen.themeName
                    )
                } else {
                    EditPostFragment.newInstance(
                            screen.editPostForm,
                            screen.themeName
                    )
                }
                createFragment(EditPostFragment::class.java, args)
            }
            is Screen.Favorites -> createFragment(FavoritesFragment::class.java, args)
            is Screen.Forum -> {
                createFragment(ForumFragment::class.java, args.apply {
                    putInt(ForumFragment.ARG_FORUM_ID, screen.forumId)
                })
            }
            is Screen.History -> createFragment(HistoryFragment::class.java, args)
            is Screen.Mentions -> createFragment(MentionsFragment::class.java, args)
            is Screen.ArticleList -> createFragment(NewsMainFragment::class.java, args)
            is Screen.ArticleDetail -> {
                createFragment(NewsDetailsFragment::class.java, args.apply {
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
            is Screen.Notes -> createFragment(NotesFragment::class.java, args)
            is Screen.Announce -> {
                createFragment(AnnounceFragment::class.java, args.apply {
                    putInt(AnnounceFragment.ARG_ANNOUNCE_ID, screen.announceId)
                    putInt(AnnounceFragment.ARG_FORUM_ID, screen.forumId)
                })
            }
            is Screen.ForumRules -> createFragment(ForumRulesFragment::class.java, args)
            is Screen.GoogleCaptcha -> createFragment(GoogleCaptchaFragment::class.java, args)
            is Screen.Profile -> {
                createFragment(ProfileFragment::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.profileUrl)
                })
            }
            is Screen.QmsContacts -> createFragment(QmsContactsFragment::class.java, args)
            is Screen.QmsBlackList -> createFragment(QmsBlackListFragment::class.java, args)
            is Screen.QmsThemes -> {
                createFragment(QmsThemesFragment::class.java, args.apply {
                    putInt(QmsThemesFragment.USER_ID_ARG, screen.userId)
                    putString(QmsThemesFragment.USER_AVATAR_ARG, screen.avatarUrl)
                })
            }
            is Screen.QmsChat -> {
                createFragment(QmsChatFragment::class.java, args.apply {
                    putInt(QmsChatFragment.THEME_ID_ARG, screen.themeId)
                    putInt(QmsChatFragment.USER_ID_ARG, screen.userId)
                    putString(QmsChatFragment.USER_NICK_ARG, screen.userNick)
                    putString(QmsChatFragment.USER_AVATAR_ARG, screen.avatarUrl)
                    putString(QmsChatFragment.THEME_TITLE_ARG, screen.themeTitle)
                })
            }
            is Screen.Reputation -> {
                createFragment(ReputationFragment::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.reputationUrl)
                })
            }
            is Screen.Search -> {
                createFragment(SearchFragment::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.searchUrl)
                })
            }
            is Screen.Theme -> {
                createFragment(ThemeFragmentWeb::class.java, args.apply {
                    putString(TabFragment.ARG_TAB, screen.themeUrl)
                })
            }
            is Screen.Topics -> {
                createFragment(TopicsFragment::class.java, args.apply {
                    putInt(TopicsFragment.TOPICS_ID_ARG, screen.forumId)
                })
            }
            else -> {
                throw Exception("What is screen: \"$screen\" bro? I don't know this screen. Look at this beautiful exception ))0)")
            }
        }
    }

    fun findClassByScreen(screen: Screen): Class<out TabFragment> {
        return when (screen) {
            is Screen.Auth -> AuthFragment::class.java
            is Screen.DevDbDevices -> DevicesFragment::class.java
            is Screen.DevDbBrands -> BrandsFragment::class.java
            is Screen.DevDbDevice -> DeviceFragment::class.java
            is Screen.DevDbSearch -> DevDbSearchFragment::class.java
            is Screen.EditPost -> EditPostFragment::class.java
            is Screen.Favorites -> FavoritesFragment::class.java
            is Screen.Forum -> ForumFragment::class.java
            is Screen.History -> HistoryFragment::class.java
            is Screen.Mentions -> MentionsFragment::class.java
            is Screen.ArticleList -> NewsMainFragment::class.java
            is Screen.ArticleDetail -> NewsDetailsFragment::class.java
            is Screen.Notes -> NotesFragment::class.java
            is Screen.Announce -> AnnounceFragment::class.java
            is Screen.ForumRules -> ForumRulesFragment::class.java
            is Screen.GoogleCaptcha -> GoogleCaptchaFragment::class.java
            is Screen.Profile -> ProfileFragment::class.java
            is Screen.QmsContacts -> QmsContactsFragment::class.java
            is Screen.QmsBlackList -> QmsBlackListFragment::class.java
            is Screen.QmsThemes -> QmsThemesFragment::class.java
            is Screen.QmsChat -> QmsChatFragment::class.java
            is Screen.Reputation -> ReputationFragment::class.java
            is Screen.Search -> SearchFragment::class.java
            is Screen.Theme -> ThemeFragmentWeb::class.java
            is Screen.Topics -> TopicsFragment::class.java
            else -> {
                throw Exception("Not found class by screen: \"$screen\"")
            }
        }
    }
}