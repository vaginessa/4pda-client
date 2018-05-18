package forpdateam.ru.forpda.presentation

import forpdateam.ru.forpda.common.IntentHandler

class SystemLinkHandler : ISystemLinkHandler {
    override fun handle(url: String) {
        IntentHandler.externalIntent(url)
    }

    override fun handleDownload(url: String, fileName: String?) {
        IntentHandler.handleDownload(fileName, url)
    }
}