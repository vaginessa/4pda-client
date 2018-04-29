package forpdateam.ru.forpda.presentation.search

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.apirx.ForumUsersCache
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import forpdateam.ru.forpda.ui.TemplateManager
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

class SearchTemplate(
        private val templateManager: TemplateManager
) {

    private val firstLetter = Pattern.compile("([a-zA-Zа-яА-Я])")

    fun mapEntity(page: SearchResult): SearchResult = page.apply { html = mapString(page) }

    private fun mapString(page: SearchResult): String {
        val forumUsers = ArrayList<ForumUser>()
        for (post in page.items) {
            val forumUser = ForumUser()
            forumUser.id = post.userId
            forumUser.nick = post.nick
            forumUser.avatar = post.avatar
        }
        ForumUsersCache.saveUsers(forumUsers)

        val memberId = ClientHelper.getUserId()
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_SEARCH)

        template.apply {
            templateManager.fillStaticStrings(template)
            val authorized = ClientHelper.getAuthState()
            val prevDisabled = page.pagination.current <= 1
            val nextDisabled = page.pagination.current == page.pagination.all

            setVariableOpt("style_type", templateManager.getThemeType())

            setVariableOpt("all_pages_int", page.pagination.all)
            setVariableOpt("posts_on_page_int", page.pagination.perPage)
            setVariableOpt("current_page_int", page.pagination.current)
            setVariableOpt("authorized_bool", java.lang.Boolean.toString(authorized))
            setVariableOpt("member_id_int", ClientHelper.getUserId())


            setVariableOpt("body_type", "search")
            setVariableOpt("navigation_disable", TempHelper.getDisableStr(prevDisabled && nextDisabled))
            setVariableOpt("first_disable", TempHelper.getDisableStr(prevDisabled))
            setVariableOpt("prev_disable", TempHelper.getDisableStr(prevDisabled))
            setVariableOpt("next_disable", TempHelper.getDisableStr(nextDisabled))
            setVariableOpt("last_disable", TempHelper.getDisableStr(nextDisabled))

            val isEnableAvatars = Preferences.Theme.isShowAvatars(null)
            setVariableOpt("enable_avatars_bool", java.lang.Boolean.toString(isEnableAvatars))
            setVariableOpt("enable_avatars", if (isEnableAvatars) "show_avatar" else "hide_avatar")
            setVariableOpt("avatar_type", if (Preferences.Theme.isCircleAvatars(null)) "circle_avatar" else "square_avatar")


            var letterMatcher: Matcher? = null
            for (post in page.items) {
                setVariableOpt("topic_id", post.topicId)
                setVariableOpt("post_title", post.title)

                setVariableOpt("user_online", if (post.isOnline) "online" else "")
                setVariableOpt("post_id", post.id)
                setVariableOpt("user_id", post.userId)

                //Post header
                setVariableOpt("avatar", post.avatar)
                setVariableOpt("none_avatar", if (post.avatar.isEmpty()) "none_avatar" else "")

                letterMatcher = letterMatcher?.reset(post.nick) ?: firstLetter.matcher(post.nick)
                val letter: String = letterMatcher?.run {
                    find()
                    group(1)
                } ?: post.nick.substring(0, 1)

                setVariableOpt("nick_letter", letter)
                setVariableOpt("nick", ApiUtils.htmlEncode(post.nick))
                //t.setVariableOpt("curator", false ? "curator" : "");
                setVariableOpt("group_color", post.groupColor)
                setVariableOpt("group", post.group)
                setVariableOpt("reputation", post.reputation)
                setVariableOpt("date", post.date)
                //t.setVariableOpt("number", post.getNumber());

                //Post body
                setVariableOpt("body", post.body)

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

                addBlockOpt("post")
            }
        }

        val result = template.generateOutput()
        template.reset()

        return result
    }

}