package forpdateam.ru.forpda.model.repository.temp

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import biz.source_code.miniTemplator.MiniTemplator
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.apirx.ForumUsersCache
import forpdateam.ru.forpda.client.ClientHelper
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
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

    @StringRes
    fun getTypeString(type: ProfileModel.ContactType): Int {
        when (type) {
            ProfileModel.ContactType.QMS -> return R.string.profile_contact_qms
            ProfileModel.ContactType.WEBSITE -> return R.string.profile_contact_site
            ProfileModel.ContactType.ICQ -> return R.string.profile_contact_icq
            ProfileModel.ContactType.TWITTER -> return R.string.profile_contact_twitter
            ProfileModel.ContactType.JABBER -> return R.string.profile_contact_jabber
            ProfileModel.ContactType.VKONTAKTE -> return R.string.profile_contact_vk
            ProfileModel.ContactType.GOOGLE_PLUS -> return R.string.profile_contact_google_plus
            ProfileModel.ContactType.FACEBOOK -> return R.string.profile_contact_facebook
            ProfileModel.ContactType.INSTAGRAM -> return R.string.profile_contact_instagram
            ProfileModel.ContactType.MAIL_RU -> return R.string.profile_contact_mail_ru
            ProfileModel.ContactType.TELEGRAM -> return R.string.profile_contact_telegram
            ProfileModel.ContactType.WINDOWS_LIVE -> return R.string.profile_contact_windows_live
            else -> return R.string.undefined
        }
    }

    @StringRes
    fun getTypeString(type: ProfileModel.InfoType): Int {
        when (type) {
            ProfileModel.InfoType.REG_DATE -> return R.string.profile_info_reg
            ProfileModel.InfoType.ALERTS -> return R.string.profile_info_alerts
            ProfileModel.InfoType.ONLINE_DATE -> return R.string.profile_info_last_online
            ProfileModel.InfoType.GENDER -> return R.string.profile_info_gender
            ProfileModel.InfoType.BIRTHDAY -> return R.string.profile_info_birthday
            ProfileModel.InfoType.USER_TIME -> return R.string.profile_info_user_time
            ProfileModel.InfoType.CITY -> return R.string.profile_info_city
            else -> return R.string.undefined
        }
    }

    @StringRes
    fun getTypeString(type: ProfileModel.StatType): Int {
        when (type) {
            ProfileModel.StatType.SITE_KARMA -> return R.string.profile_stat_site_karma
            ProfileModel.StatType.SITE_POSTS -> return R.string.profile_stat_site_posts
            ProfileModel.StatType.SITE_COMMENTS -> return R.string.profile_stat_site_comments
            ProfileModel.StatType.FORUM_REPUTATION -> return R.string.profile_stat_forum_reputation
            ProfileModel.StatType.FORUM_TOPICS -> return R.string.profile_stat_forum_topics
            ProfileModel.StatType.FORUM_POSTS -> return R.string.profile_stat_forum_posts
            else -> return R.string.undefined
        }
    }

    @DrawableRes
    fun getContactIcon(type: ProfileModel.ContactType): Int {
        when (type) {
            ProfileModel.ContactType.QMS -> return R.drawable.contact_qms
            ProfileModel.ContactType.WEBSITE -> return R.drawable.contact_site
            ProfileModel.ContactType.ICQ -> return R.drawable.contact_icq
            ProfileModel.ContactType.TWITTER -> return R.drawable.contact_twitter
            ProfileModel.ContactType.JABBER -> return R.drawable.contact_jabber
            ProfileModel.ContactType.VKONTAKTE -> return R.drawable.contact_vk
            ProfileModel.ContactType.GOOGLE_PLUS -> return R.drawable.contact_google_plus
            ProfileModel.ContactType.FACEBOOK -> return R.drawable.contact_facebook
            ProfileModel.ContactType.INSTAGRAM -> return R.drawable.contact_instagram
            ProfileModel.ContactType.MAIL_RU -> return R.drawable.contact_mail_ru
            ProfileModel.ContactType.TELEGRAM -> return R.drawable.contact_telegram
        /*case WINDOWS_LIVE:
                return R.drawable.contact_site;*/
            else -> return R.drawable.contact_site
        }
    }
}