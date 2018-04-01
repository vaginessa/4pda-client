package forpdateam.ru.forpda.model.repository.temp

import biz.source_code.miniTemplator.MiniTemplator
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.apirx.ForumUsersCache
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 30.03.18.
 */
object TempHelper {
    private val firstLetter = Pattern.compile("([a-zA-Zа-яА-Я])")
    fun transform(page: ThemePage, withHtml: Boolean): ThemePage {
        if (withHtml) {

            val forumUsers = ArrayList<ForumUser>()
            for (post in page.posts) {
                val forumUser = ForumUser()
                forumUser.id = post.userId
                forumUser.nick = post.nick
                forumUser.avatar = post.avatar
            }
            ForumUsersCache.saveUsers(forumUsers)

            val memberId = ClientHelper.getUserId()
            val t = App.get().getTemplate(App.TEMPLATE_THEME)
            App.setTemplateResStrings(t)
            val authorized = ClientHelper.getAuthState()
            val prevDisabled = page.pagination.current <= 1
            val nextDisabled = page.pagination.current == page.pagination.all

            t!!.setVariableOpt("style_type", App.get().cssStyleType)

            t.setVariableOpt("topic_title", ApiUtils.htmlEncode(page.title))
            t.setVariableOpt("topic_description", ApiUtils.htmlEncode(page.desc))
            t.setVariableOpt("topic_url", page.url)

            t.setVariableOpt("all_pages_int", page.pagination.all)
            t.setVariableOpt("posts_on_page_int", page.pagination.perPage)
            t.setVariableOpt("current_page_int", page.pagination.current)

            t.setVariableOpt("authorized_bool", java.lang.Boolean.toString(authorized))
            t.setVariableOpt("is_curator_bool", java.lang.Boolean.toString(page.isCurator))
            t.setVariableOpt("member_id_int", ClientHelper.getUserId())
            t.setVariableOpt("elem_to_scroll", page.anchor)
            t.setVariableOpt("body_type", "topic")

            t.setVariableOpt("navigation_disable", getDisableStr(prevDisabled && nextDisabled))
            t.setVariableOpt("first_disable", getDisableStr(prevDisabled))
            t.setVariableOpt("prev_disable", getDisableStr(prevDisabled))
            t.setVariableOpt("next_disable", getDisableStr(nextDisabled))
            t.setVariableOpt("last_disable", getDisableStr(nextDisabled))

            t.setVariableOpt("in_favorite_bool", java.lang.Boolean.toString(page.isInFavorite))
            val isEnableAvatars = Preferences.Theme.isShowAvatars(null)
            t.setVariableOpt("enable_avatars_bool", java.lang.Boolean.toString(isEnableAvatars))
            t.setVariableOpt("enable_avatars", if (isEnableAvatars) "show_avatar" else "hide_avatar")
            t.setVariableOpt("avatar_type", if (Preferences.Theme.isCircleAvatars(null)) "circle_avatar" else "square_avatar")


            var hatPostId = 0
            if (!page.posts.isEmpty()) {
                hatPostId = page.posts[0].id
            }
            var letterMatcher: Matcher? = null
            for (post in page.posts) {
                t.setVariableOpt("user_online", if (post.isOnline) "online" else "")
                t.setVariableOpt("post_id", post.id)
                t.setVariableOpt("user_id", post.userId)

                //Post header
                //t.setVariableOpt("avatar", post.getAvatar().isEmpty() ? "file:///android_asset/av.png" : "https://s.4pda.to/forum/uploads/".concat(post.getAvatar()));
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
                t.setVariableOpt("curator", if (post.isCurator) "curator" else "")
                t.setVariableOpt("group_color", post.groupColor)
                t.setVariableOpt("group", post.group)
                t.setVariableOpt("reputation", post.reputation)
                t.setVariableOpt("date", post.date)
                t.setVariableOpt("number", post.number)

                //Post body
                if (page.posts.size > 1 && hatPostId == post.id) {
                    val hatOpened = prevDisabled || page.isHatOpen
                    t.setVariableOpt("hat_state_class", if (prevDisabled || page.isHatOpen) "open" else "close")
                    //t.setVariableOpt("hat_body_state", prevDisabled || page.isHatOpen() ? "" : "hidden");
                    t.addBlockOpt("hat_button")
                    t.addBlockOpt("hat_content_start")
                    t.addBlockOpt("hat_content_end")
                } else {
                    t.setVariableOpt("hat_state_class", "")
                }
                t.setVariableOpt("body", post.body)

                //Post footer

                if (post.canReport() && authorized)
                    t.addBlockOpt("report_block")
                if (page.canQuote() && authorized && post.userId != memberId)
                    t.addBlockOpt("reply_block")
                if (authorized && post.userId != memberId)
                    t.addBlockOpt("vote_block")
                if (post.canDelete() && authorized)
                    t.addBlockOpt("delete_block")
                if (post.canEdit() && authorized)
                    t.addBlockOpt("edit_block")

                t.addBlockOpt("post")
            }

            //Poll block
            if (page.poll != null) {
                t.setVariableOpt("poll_state_class", if (page.isPollOpen) "open" else "close")
                //t.setVariableOpt("poll_body_state", page.isPollOpen() ? "" : "hidden");
                val poll = page.poll
                val isResult = poll.isResult
                t.setVariableOpt("poll_type", if (isResult) "result" else "default")
                t.setVariableOpt("poll_title", if (poll.title.isEmpty() || poll.title == "-") App.get().getString(R.string.poll) else poll.title)

                for (question in poll.questions) {
                    t.setVariableOpt("question_title", question.title)

                    for (questionItem in question.questionItems) {
                        t.setVariableOpt("question_item_title", questionItem.title)

                        if (isResult) {
                            t.setVariableOpt("question_item_votes", questionItem.votes)
                            t.setVariableOpt("question_item_percent", java.lang.Float.toString(questionItem.percent))
                            t.addBlockOpt("poll_result_item")
                        } else {
                            t.setVariableOpt("question_item_type", questionItem.type)
                            t.setVariableOpt("question_item_name", questionItem.name)
                            t.setVariableOpt("question_item_value", questionItem.value)
                            t.addBlockOpt("poll_default_item")
                        }
                    }
                    t.addBlockOpt("poll_question_block")
                }
                t.setVariableOpt("poll_votes_count", poll.votesCount)
                if (poll.haveButtons()) {
                    if (poll.haveVoteButton())
                        t.addBlockOpt("poll_vote_button")
                    if (poll.haveShowResultsButton())
                        t.addBlockOpt("poll_show_results_button")
                    if (poll.haveShowPollButton())
                        t.addBlockOpt("poll_show_poll_button")
                    t.addBlockOpt("poll_buttons")
                }
                t.addBlockOpt("poll_block")
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

    fun getDisableStr(b: Boolean): String {
        return if (b) "disabled" else ""
    }


    /* QMS */
    fun interceptContacts(contacts: ArrayList<QmsContact>): ArrayList<QmsContact> {
        val forumUsers = ArrayList<ForumUser>()
        for (post in contacts) {
            val forumUser = ForumUser()
            forumUser.id = post.id
            forumUser.nick = post.nick
            forumUser.avatar = post.avatar
        }
        ForumUsersCache.saveUsers(forumUsers)
        return contacts
    }

    fun transform(chatModel: QmsChatModel, withHtml: Boolean): QmsChatModel {
        if (withHtml) {
            val t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT)
            App.setTemplateResStrings(t)
            t!!.setVariableOpt("style_type", App.get().cssStyleType)
            t.setVariableOpt("chat_title", ApiUtils.htmlEncode(chatModel.title))
            t.setVariableOpt("chatId", chatModel.themeId)
            t.setVariableOpt("userId", chatModel.userId)
            t.setVariableOpt("nick", chatModel.nick)
            t.setVariableOpt("avatarUrl", chatModel.avatarUrl)

            val endIndex = chatModel.messages.size
            val startIndex = Math.max(endIndex - 30, 0)
            chatModel.showedMessIndex = startIndex
            val messTemp = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS)
            App.setTemplateResStrings(t)
            generateMess(messTemp, chatModel.messages, startIndex, endIndex)
            t.setVariableOpt("messages", messTemp!!.generateOutput())
            messTemp.reset()
            chatModel.html = t.generateOutput()
            t.reset()
        }
        return chatModel
    }

    fun generateMess(t: MiniTemplator?, messages: List<QmsMessage>, start: Int, end: Int): MiniTemplator? {
        for (i in start until end) {
            val mess = messages[i]
            generateMess(t, mess)
        }
        return t
    }

     fun generateMess(t: MiniTemplator?, mess: QmsMessage): MiniTemplator {
        if (mess.isDate) {
            t!!.setVariableOpt("date", mess.date)
            t.addBlockOpt("date")
        } else {
            t!!.setVariableOpt("from_class", if (mess.isMyMessage) "our" else "his")
            t.setVariableOpt("unread_class", if (mess.readStatus) "" else "unread")
            t.setVariableOpt("mess_id", mess.id)
            t.setVariableOpt("content", mess.content)
            t.setVariableOpt("time", mess.time)
            t.addBlockOpt("mess")
        }
        t.addBlockOpt("item")

        return t
    }

    fun transformMessageSrc(messagesSrcIn: String): String {
        var messagesSrc = messagesSrcIn
        messagesSrc = messagesSrc.replace("\n".toRegex(), "").replace("'".toRegex(), "&apos;")
        messagesSrc = JSONObject.quote(messagesSrc)
        messagesSrc = messagesSrc.substring(1, messagesSrc.length - 1)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("src", messagesSrc)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return messagesSrc
    }
}