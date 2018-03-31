package forpdateam.ru.forpda.model.repository.search

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.data.remote.api.search.SearchApi
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.apirx.ForumUsersCache
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import io.reactivex.Observable
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 01.01.18.
 */

class SearchRepository(
        private val schedulers: SchedulersProvider,
        private val searchApi: SearchApi
) {

    private val firstLetter = Pattern.compile("([a-zA-Zа-яА-Я])")

    fun getSearch(settings: SearchSettings, withHtml: Boolean): Observable<SearchResult> = Observable
            .fromCallable { searchApi.getSearch(settings) }
            .map { transform(it, withHtml) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    private fun transform(page: SearchResult, withHtml: Boolean): SearchResult {
        if (withHtml) {
            val forumUsers = ArrayList<ForumUser>()
            for (post in page.items) {
                val forumUser = ForumUser()
                forumUser.id = post.userId
                forumUser.nick = post.nick
                forumUser.avatar = post.avatar
            }
            ForumUsersCache.saveUsers(forumUsers)

            val memberId = ClientHelper.getUserId()
            val t = App.get().getTemplate(App.TEMPLATE_SEARCH)
            App.setTemplateResStrings(t)
            val authorized = ClientHelper.getAuthState()
            val prevDisabled = page.pagination.current <= 1
            val nextDisabled = page.pagination.current == page.pagination.all

            t!!.setVariableOpt("style_type", App.get().cssStyleType)

            t.setVariableOpt("all_pages_int", page.pagination.all)
            t.setVariableOpt("posts_on_page_int", page.pagination.perPage)
            t.setVariableOpt("current_page_int", page.pagination.current)
            t.setVariableOpt("authorized_bool", java.lang.Boolean.toString(authorized))
            t.setVariableOpt("member_id_int", ClientHelper.getUserId())


            t.setVariableOpt("body_type", "search")
            t.setVariableOpt("navigation_disable", TempHelper.getDisableStr(prevDisabled && nextDisabled))
            t.setVariableOpt("first_disable", TempHelper.getDisableStr(prevDisabled))
            t.setVariableOpt("prev_disable", TempHelper.getDisableStr(prevDisabled))
            t.setVariableOpt("next_disable", TempHelper.getDisableStr(nextDisabled))
            t.setVariableOpt("last_disable", TempHelper.getDisableStr(nextDisabled))

            val isEnableAvatars = Preferences.Theme.isShowAvatars(null)
            t.setVariableOpt("enable_avatars_bool", java.lang.Boolean.toString(isEnableAvatars))
            t.setVariableOpt("enable_avatars", if (isEnableAvatars) "show_avatar" else "hide_avatar")
            t.setVariableOpt("avatar_type", if (Preferences.Theme.isCircleAvatars(null)) "circle_avatar" else "square_avatar")


            var letterMatcher: Matcher? = null
            for (post in page.items) {
                t.setVariableOpt("topic_id", post.topicId)
                t.setVariableOpt("post_title", post.title)

                t.setVariableOpt("user_online", if (post.isOnline) "online" else "")
                t.setVariableOpt("post_id", post.id)
                t.setVariableOpt("user_id", post.userId)

                //Post header
                t.setVariableOpt("avatar", post.avatar)
                t.setVariableOpt("none_avatar", if (post.avatar.isEmpty()) "none_avatar" else "")

                if (letterMatcher != null) {
                    letterMatcher = letterMatcher.reset(post.nick)
                } else {
                    letterMatcher = firstLetter.matcher(post.nick)
                }
                var letter: String? = null
                if (letterMatcher!!.find()) {
                    letter = letterMatcher.group(1)
                } else {
                    letter = post.nick.substring(0, 1)
                }
                t.setVariableOpt("nick_letter", letter)
                t.setVariableOpt("nick", ApiUtils.htmlEncode(post.nick))
                //t.setVariableOpt("curator", false ? "curator" : "");
                t.setVariableOpt("group_color", post.groupColor)
                t.setVariableOpt("group", post.group)
                t.setVariableOpt("reputation", post.reputation)
                t.setVariableOpt("date", post.date)
                //t.setVariableOpt("number", post.getNumber());

                //Post body
                t.setVariableOpt("body", post.body)

                //Post footer

                /*if (post.canReport() && authorized)
                    t.addBlockOpt("report_block");
                if (page.canQuote() && authorized && post.getUserId() != memberId)
                    t.addBlockOpt("reply_block");
                if (authorized && post.getUserId() != memberId)
                    t.addBlockOpt("vote_block");
                if (post.canDelete() && authorized)
                    t.addBlockOpt("delete_block");
                if (post.canEdit() && authorized)
                    t.addBlockOpt("edit_block");*/

                t.addBlockOpt("post")
            }

            page.html = t.generateOutput()
            t.reset()
        }

        /*final String veryLongString = page.getHtml();

        int maxLogSize = 1000;
        for (int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > veryLongString.length() ? veryLongString.length() : end;
            Log.v("FORPDA_LOG", veryLongString.substring(start, end));
        }*/

        return page
    }
}
