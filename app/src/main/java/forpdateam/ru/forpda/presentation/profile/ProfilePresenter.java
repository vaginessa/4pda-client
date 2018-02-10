package forpdateam.ru.forpda.presentation.profile;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
public class ProfilePresenter extends BasePresenter<ProfileView> {

    private String profileUrl;
    private ProfileModel currentData;
    private ProfileRepository profileRepository;

    public ProfilePresenter(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadProfile();
    }

    private void loadProfile() {
        Disposable disposable
                = profileRepository.loadProfile(profileUrl)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(profileModel -> {
                    currentData = profileModel;
                    getViewState().showProfile(profileModel);
                }, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void saveNote(String note) {
        Disposable disposable
                = profileRepository.saveNote(note)
                .subscribe(success -> getViewState().onSaveNote(success), this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void copyUrl() {
        Utils.copyToClipBoard(profileUrl);
    }

    public void navigateToQms() {
        IntentHandler.handle(currentData.getContacts().get(0).getUrl());
    }
}
