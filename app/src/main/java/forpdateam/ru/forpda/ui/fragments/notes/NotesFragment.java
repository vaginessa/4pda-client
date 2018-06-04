package forpdateam.ru.forpda.ui.fragments.notes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.FilePickHelper;
import forpdateam.ru.forpda.entity.app.notes.NoteItem;
import forpdateam.ru.forpda.model.data.remote.api.RequestFile;
import forpdateam.ru.forpda.presentation.notes.NotesPresenter;
import forpdateam.ru.forpda.presentation.notes.NotesView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment;
import forpdateam.ru.forpda.ui.fragments.notes.adapters.NotesAdapter;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;

/**
 * Created by radiationx on 06.09.17.
 */

public class NotesFragment extends RecyclerFragment implements NotesView, NotesAdapter.OnItemClickListener<NoteItem> {
    private NotesAdapter adapter;
    private DynamicDialogMenu<NotesFragment, NoteItem> dialogMenu = new DynamicDialogMenu<>();

    @InjectPresenter
    NotesPresenter presenter;

    @ProvidePresenter
    NotesPresenter providePresenter() {
        return new NotesPresenter(
                App.get().Di().getNotesRepository(),
                App.get().Di().getRouter(),
                App.get().Di().getLinkHandler()
        );
    }

    public NotesFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_notes));
        configuration.setUseCache(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCardsBackground();
        adapter = new NotesAdapter();
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout.setOnRefreshListener(() -> presenter.loadNotes());
        recyclerView.addItemDecoration(new DevicesFragment.SpacingItemDecoration(App.px8, false));

        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.edit), (context, data) -> presenter.editNote(data));
        dialogMenu.addItem(getString(R.string.delete), (context, data) -> presenter.deleteNote(data.getId()));
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu
                .add(R.string.add)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_add))
                .setOnMenuItemClickListener(item -> {
                    presenter.addNote();
                    return true;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu
                .add(R.string.import_s)
                .setOnMenuItemClickListener(item -> {
                    App.get().checkStoragePermission(() -> {
                        startActivityForResult(FilePickHelper.pickFile(false), REQUEST_PICK_FILE);
                    }, App.getActivity());
                    return true;
                });
        menu
                .add(R.string.export_s)
                .setOnMenuItemClickListener(item -> {
                    App.get().checkStoragePermission(() -> presenter.exportNotes(), App.getActivity());
                    return true;
                });

    }

    @Override
    public void showNotes(@NotNull List<? extends NoteItem> items) {
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_bookmark)
                        .setTitle(R.string.funny_notes_nodata_title);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }
        adapter.addAll(items);
    }

    @Override
    public void showNotesEditPopup(@NotNull NoteItem item) {
        new NotesAddPopup(getContext(), item);
    }

    @Override
    public void showNotesAddPopup() {
        new NotesAddPopup(getContext(), null);
    }

    @Override
    public void onImportNotes() {
        Toast.makeText(getContext(), "Заметки успешно импортированы", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExportNotes(@NotNull String path) {
        Toast.makeText(getContext(), "Заметки успешно экспортированы в " + path, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            if (requestCode == REQUEST_PICK_FILE) {
                List<RequestFile> files = FilePickHelper.onActivityResult(getContext(), data);
                RequestFile file = files.get(0);
                if (file.getFileName().matches("[\\s\\S]*?\\.json$")) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(file.getFileStream()));
                    StringBuilder total = new StringBuilder();
                    String line;
                    try {
                        while ((line = r.readLine()) != null) {
                            total.append(line).append('\n');
                        }
                        r.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Ошибка при чтении файла", Toast.LENGTH_SHORT).show();
                    }
                    presenter.importNotes(total.toString());
                } else {
                    Toast.makeText(getContext(), "Файл имеет неправильное расширение", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_SAVE_FILE) {

            }
        }
    }

    @Override
    public void onItemClick(NoteItem item) {
        presenter.onItemClick(item);
    }

    @Override
    public boolean onItemLongClick(NoteItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), NotesFragment.this, item);
        return true;
    }
}
