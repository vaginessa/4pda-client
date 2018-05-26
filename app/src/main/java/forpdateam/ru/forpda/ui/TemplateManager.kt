package forpdateam.ru.forpda.ui

import android.content.Context
import biz.source_code.miniTemplator.MiniTemplator
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

class TemplateManager(
        private val context: Context,
        private val themeHolder: AppThemeHolder
) {

    companion object {
        const val TEMPLATE_THEME = "theme"
        const val TEMPLATE_SEARCH = "search"
        const val TEMPLATE_QMS_CHAT = "qms_chat"
        const val TEMPLATE_QMS_CHAT_MESS = "qms_chat_mess"
        const val TEMPLATE_NEWS = "news"
        const val TEMPLATE_FORUM_RULES = "forum_rules"
        const val TEMPLATE_ANNOUNCE = "announce"
    }

    private val staticStrings = mutableMapOf<String, String>()
    private val templates = mutableMapOf<String, MiniTemplator>()

    fun setStaticStrings(strings: Map<String, String>) {
        staticStrings.clear()
        staticStrings.putAll(strings)
    }

    fun getThemeType(): String {
        return if (themeHolder.getTheme() == "dark") "dark" else "light"
    }

    fun fillStaticStrings(template: MiniTemplator): MiniTemplator = template.apply {
        variables.forEach { entry ->
            staticStrings[entry.key]?.let {
                setVariable(entry.key, it)
            }
        }
    }

    fun getTemplate(name: String): MiniTemplator = templates[name]
            ?: findTemplate(name).apply { templates[name] = this }

    private fun findTemplate(name: String): MiniTemplator = try {
        val stream = context.assets.open("template_$name.html")
        MiniTemplator.Builder().build(stream, Charset.forName("utf-8"))
    } catch (ex: Exception) {
        ex.printStackTrace()
        MiniTemplator.Builder().build(ByteArrayInputStream("Template error!".toByteArray(Charset.forName("utf-8"))), Charset.forName("utf-8"))
    }

}