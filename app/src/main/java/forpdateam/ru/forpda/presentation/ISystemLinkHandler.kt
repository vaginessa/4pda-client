package forpdateam.ru.forpda.presentation

interface ISystemLinkHandler {
    fun handle(url: String)
    fun handleDownload(url: String, fileName: String? = null)
}