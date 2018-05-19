package forpdateam.ru.forpda.common;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest;
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse;
import forpdateam.ru.forpda.presentation.ILinkHandler;
import forpdateam.ru.forpda.presentation.IRouter;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.brands.BrandsFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.device.DeviceFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.ui.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.ui.fragments.news.details.NewsDetailsFragment;
import forpdateam.ru.forpda.ui.fragments.news.main.NewsMainFragment;
import forpdateam.ru.forpda.ui.fragments.other.AnnounceFragment;
import forpdateam.ru.forpda.ui.fragments.other.ForumRulesFragment;
import forpdateam.ru.forpda.ui.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.ui.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.ui.fragments.qms.QmsThemesFragment;
import forpdateam.ru.forpda.ui.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.ui.fragments.reputation.ReputationFragment;
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb;
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radiationx on 04.08.16.
 */
public class IntentHandler {

    public static boolean handle(String url) {
        ILinkHandler linkHandler = App.get().Di().getLinkHandler();
        IRouter router = App.get().Di().getRouter();
        return linkHandler.handle(url, router);
    }


    public static void handleDownload(String url) {
        ISystemLinkHandler linkHandler = App.get().Di().getSystemLinkHandler();
        linkHandler.handleDownload(url, null);
    }

}
