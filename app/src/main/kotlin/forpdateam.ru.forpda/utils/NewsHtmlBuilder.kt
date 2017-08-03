package forpdateam.ru.forpda.utils

open class HtmlBuilder {
    var html: StringBuilder? = null
        private set

    fun beginHtml(title: String) {
        html = StringBuilder()
        html?.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">")
        html?.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n")
        html?.append("<head>\n")
        html?.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n")
        html?.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n")
        html?.let { addStyleSheetLink(it) }
        html?.append("<title>")?.append(title)?.append("</title>\n")
        html?.append("</head>\n")
    }

    open fun addStyleSheetLink(sb: StringBuilder) {
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n")
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n")
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n")
    }

    fun append(str: String): HtmlBuilder {
        html!!.append(str)
        return this
    }

    @JvmOverloads
    fun beginBody(id: String, isImage: Boolean = true) {
        //        m_Body.append("<body id=\"").append(id).append("\" class=\"modification ")
        //                .append(isImage ? "" : "noimages ")
        //                .append(Preferences.Media.INSTANCE.getPlayGif() ? "ongpuimg \" " : "\" ");
    }

    fun endBody(): HtmlBuilder {
        html?.append("</body>\n")
        return this
    }

    fun endHtml() {
        html?.append("</html>")

    }
}

class NewsHtmlBuilder : HtmlBuilder() {

    companion object {

        fun transformBody(bodyOrig: String): String {
            var body = bodyOrig
            val builder = NewsHtmlBuilder()
            builder.beginHtml("News")
            builder.beginBody("news", true)
            builder.append("<div id=\"news\">")
            body = body.replace("\"//".toRegex(), "\"http://")
            builder.append(body)
            builder.append("</div>")
            builder.endBody()
            builder.endHtml()
            return builder.html!!.toString()
        }

    }

    override fun addStyleSheetLink(sb: StringBuilder) {
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/news/news.css\"/>\n")
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/news/youtube_video.css\"/>\n")
    }
}