package forpdateam.ru.forpda.presentation

import android.net.Uri
import android.util.Log
import forpdateam.ru.forpda.common.MimeTypeUtil
import java.net.URLDecoder
import java.util.regex.Pattern

/**
 * Created by radiationx on 03.02.18.
 */
class LinkHandler(
        private val systemLinkHandler: ISystemLinkHandler
) : ILinkHandler {

    companion object {
        const val LOG_TAG = "LinkHandler"
    }

    private val forumMediaPattern by lazy { Pattern.compile("https?:\\/\\/4pda\\.ru\\/forum\\/dl\\/post\\/\\d+\\/([\\s\\S]*\\.([\\s\\S]*))") }

    private val supportImagePattern by lazy { Pattern.compile("\\/\\/.*?(4pda\\.to|4pda\\.ru|ggpht\\.com|googleusercontent\\.com|windowsphone\\.com|mzstatic\\.com|savepic\\.net|savepice\\.ru|savepic\\.ru|.*?\\.ibb\\.com?)\\/[\\s\\S]*?\\.(png|jpg|jpeg|gif)") }

    private val forumLofiPattern by lazy { Pattern.compile("(?:http?s?:)?\\/\\/[\\s\\S]*?4pda\\.(?:ru|to)\\/forum\\/lofiversion\\/[^\\?]*?\\?(t|f)(\\d+)(?:-(\\d+))?") }

    private val baseFourPdaPattern by lazy { Pattern.compile("(?:http?s?:)?\\/\\/[\\s\\S]*?4pda\\.(?:ru|to)[\\s\\S]*") }

    private val sitePattern by lazy { Pattern.compile("https?:\\/\\/4pda\\.ru\\/(?:.+?p=|\\d+\\/\\d+\\/\\d+\\/|[\\w\\/]*?\\/?(newer|older)\\/)(\\d+)(?:\\/#comment(\\d+))?") }


    private fun handleDownload(url: String, name: String? = null) {
        systemLinkHandler.handleDownload(url, name)
    }

    private fun externalIntent(url: String) {
        systemLinkHandler.handle(url)
    }

    override fun handle(inputUrl: String?, router: Router?, doNavigate: Boolean): Boolean {
        var url = inputUrl.orEmpty()
        if (url.isBlank() || url == "#") {
            return false
        }
        if (url.substring(0, 2) == "//") {
            url = "https:$url"
        } else if (url.substring(0, 1) == "/") {
            url = "https://4pda.ru$url"
        }
        url = url.replace("&amp;", "&").replace("\"", "").trim({ it <= ' ' })
        Log.d(LOG_TAG, "Corrected url $url")


        if (handleMedia(url, router)) {
            return true
        }
        url = normalizeForumUrl(url)

        if (baseFourPdaPattern.matcher(url).matches()) {
            val uri = Uri.parse(url.toLowerCase())
            Log.d(LOG_TAG, "Compare uri/url " + uri.toString() + " : " + url)

            if (!uri.pathSegments.isEmpty()) {
                when (uri.pathSegments[0]) {
                    "pages" -> if (handlePages(uri, router)) {
                        return true
                    }
                    "forum" -> if (handleForum(uri, router)) {
                        return true
                    }
                    "devdb" -> if (handleDevDb(uri, router)) {
                        return true
                    }
                    else -> if (handleSite(uri, router)) {
                        return true
                    }
                }
            } else {
                if (handleSite(uri, router)) {
                    return true
                }
            }

        }

        externalIntent(url)

        return false
    }

    override fun findScreen(url: String): String? {
        return null
    }

    private fun handleForum(uri: Uri, router: Router?): Boolean {
        uri.getQueryParameter("showuser")?.also { param ->
            router?.navigateTo(Screen.Profile().apply {
                profileUrl = uri.toString()
            })
            return true
        }
        uri.getQueryParameter("showtopic")?.also { param ->
            router?.navigateTo(Screen.Theme().apply {
                themeUrl = uri.toString()
            })
            return true
        }

        uri.getQueryParameter("showforum")?.also { param ->
            router?.navigateTo(Screen.Topics().apply {
                forumId = param.toInt()
            })
            return true
        }

        uri.getQueryParameter("act")?.also { param ->
            when (param) {
                "idx" -> {
                    router?.navigateTo(Screen.Forum())
                }
                "qms" -> {
                    val qmsUserId = uri.getQueryParameter("mid")
                    val qmsThemeId = uri.getQueryParameter("t")

                    if (qmsUserId == null) {
                        router?.navigateTo(Screen.QmsContacts())
                    } else {
                        if (qmsThemeId != null) {
                            router?.navigateTo(Screen.QmsChat().apply {
                                userId = qmsUserId.toInt()
                                themeId = qmsThemeId.toInt()
                            })
                        } else {
                            router?.navigateTo(Screen.QmsThemes().apply {
                                userId = qmsUserId.toInt()
                            })
                        }
                    }
                    return true
                }
                "boardrules" -> {
                    router?.navigateTo(Screen.ForumRules())
                    return true
                }
                "announce" -> {
                    router?.navigateTo(Screen.Announce().apply {
                        uri.getQueryParameter("st")?.also {
                            announceId = it.toInt()
                        }
                        uri.getQueryParameter("f")?.also {
                            forumId = it.toInt()
                        }
                    })
                    return true
                }
                "search" -> {
                    router?.navigateTo(Screen.Search().apply {
                        searchUrl = uri.toString()
                    })
                    return true
                }
                "rep" -> {
                    router?.navigateTo(Screen.Reputation().apply {
                        reputationUrl = uri.toString()
                    })
                    return true
                }
                "findpost" -> {
                    router?.navigateTo(Screen.Theme().apply {
                        themeUrl = uri.toString()
                    })
                    return true
                }
                "fav" -> {
                    router?.navigateTo(Screen.Favorites())
                    return true
                }
                "mentions" -> {
                    router?.navigateTo(Screen.Mentions())
                    return true
                }
            }
        }
        return false
    }

    private fun handleSite(uri: Uri, router: Router?): Boolean {
        val matcher = sitePattern.matcher(uri.toString())
        if (matcher.find()) {
            router?.navigateTo(Screen.ArticleDetail().apply {
                matcher.group(2)?.also {
                    articleId = it.toInt()
                }
                matcher.group(3)?.also {
                    commentId = it.toInt()
                }
                articleUrl = uri.toString()
            })
            return true
        }
        if (!uri.pathSegments.isEmpty() && uri.pathSegments[0].contains("special")) {
            return false
        }
        if (uri.pathSegments.isEmpty()) {
            router?.navigateTo(Screen.ArticleList())
            return true
        } else if (uri.pathSegments[0].matches("news|articles|reviews|tag|software|games|review".toRegex())) {
            router?.navigateTo(Screen.ArticleList())
            return true
        }

        return false
    }

    private fun handlePages(uri: Uri, router: Router?): Boolean {
        if (uri.pathSegments.size > 1 && uri.pathSegments[1].equals("go", ignoreCase = true)) {
            uri.getQueryParameter("u")?.let {
                try {
                    URLDecoder.decode(it, "UTF-8")
                } catch (ignore: Exception) {
                    it
                }
            }?.also {
                externalIntent(it)
                return true
            }
        }
        return false
    }

    private fun handleDevDb(uri: Uri, router: Router?): Boolean {
        if (uri.pathSegments.size > 1) {
            if (uri.pathSegments[1].matches("phones|pad|ebook|smartwatch".toRegex())) {
                if (uri.pathSegments.size > 2 && !uri.pathSegments[2].matches("new|select".toRegex())) {
                    router?.navigateTo(Screen.DevDbDevices().apply {
                        categoryId = uri.pathSegments[1]
                        brandId = uri.pathSegments[2]
                    })
                    return true
                }
                router?.navigateTo(Screen.DevDbBrands().apply {
                    categoryId = uri.pathSegments[1]
                })
                return true
            } else {
                router?.navigateTo(Screen.DevDbDevice().apply {
                    deviceId = uri.pathSegments[1]
                })
                return true
            }
        } else {
            router?.navigateTo(Screen.DevDbBrands())
            return true
        }
    }

    private fun handleMedia(url: String, router: Router?): Boolean {
        val matcher = forumMediaPattern.matcher(url)
        if (matcher.find()) {
            var fullName = matcher.group(1)
            try {
                fullName = URLDecoder.decode(fullName, "CP1251")
            } catch (ignore: Exception) {
            }

            val extension = matcher.group(2)
            val isImage = MimeTypeUtil.isImage(extension)
            if (isImage) {
                router?.navigateTo(Screen.ImageViewer().apply {
                    urls.add(url)
                })
            } else {
                handleDownload(fullName, url)
            }
            return true
        } else if (supportImagePattern.matcher(url).find()) {
            router?.navigateTo(Screen.ImageViewer().apply {
                urls.add(url)
            })
            return true
        }
        return false
    }

    private fun normalizeForumUrl(inputUrl: String): String {
        val matcher = forumLofiPattern.matcher(inputUrl)
        if (matcher.find()) {
            var url = "https://4pda.ru/forum/index.php?"

            url += when (matcher.group(1)) {
                "t" -> "showtopic="
                "f" -> "showforum="
                else -> ""
            } + matcher.group(2)

            matcher.group(3)?.also {
                url += "&st=$it"
            }
            return url
        }
        return inputUrl
    }

}