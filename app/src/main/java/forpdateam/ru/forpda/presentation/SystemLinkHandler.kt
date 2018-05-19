package forpdateam.ru.forpda.presentation

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.IntentHandler
import forpdateam.ru.forpda.common.MimeTypeUtil
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.acra.ACRA

class SystemLinkHandler : ISystemLinkHandler {
    override fun handle(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK)
            App.get().startActivity(Intent.createChooser(intent, App.get().getString(R.string.open_with)).addFlags(FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            ACRA.getErrorReporter().handleException(e)
        }
    }

    override fun handleDownload(url: String, inputFileName: String?) {
        val fileName = Utils.getFileNameFromUrl(url) ?: url
        val activity = App.getActivity()
        if (activity != null) {
            AlertDialog.Builder(activity)
                    .setMessage(String.format(activity.getString(R.string.load_file), fileName))
                    .setPositiveButton(activity.getString(R.string.ok)) { dialog, which -> redirectDownload(fileName, url) }
                    .setNegativeButton(activity.getString(R.string.cancel), null)
                    .show()
        } else {
            redirectDownload(fileName, url)
        }
    }

    private fun redirectDownload(fileName: String, url: String) {
        Toast.makeText(App.getContext(), String.format(App.get().getString(R.string.perform_request_link), fileName), Toast.LENGTH_SHORT).show()
        val disposable = Observable.fromCallable { App.get().Di().webClient.request(NetworkRequest.Builder().url(url).withoutBody().build()) }
                .onErrorReturn { throwable -> NetworkResponse(null) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { response ->
                    if (response.url == null) {
                        Toast.makeText(App.getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show()
                        return@subscribe
                    }
                    try {
                        val activity = App.getActivity()
                        if (!Preferences.Main.isSystemDownloader(null) || activity == null) {
                            externalDownloader(response.redirect)
                        } else {
                            val checkAction = {
                                Toast.makeText(App.getContext(), String.format(App.get().getString(R.string.perform_request_link), fileName), Toast.LENGTH_SHORT).show()
                                try {
                                    systemDownloader(fileName, response.redirect)
                                } catch (exception: Exception) {
                                    Toast.makeText(App.getContext(), R.string.perform_loading_error, Toast.LENGTH_SHORT).show()
                                    externalDownloader(response.redirect)
                                }
                            }
                            App.get().checkStoragePermission(checkAction, activity)
                        }
                    } catch (ex: Exception) {
                        ACRA.getErrorReporter().handleException(ex)
                    }
                }
    }

    private fun systemDownloader(fileName: String, url: String) {
        val dm = App.getContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setMimeType(MimeTypeUtil.getType(fileName))

        dm.enqueue(request)
    }

    private fun externalDownloader(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK)
            App.get().startActivity(Intent.createChooser(intent, App.get().getString(R.string.load_with)).addFlags(FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            ACRA.getErrorReporter().handleException(e)
        }

    }
}