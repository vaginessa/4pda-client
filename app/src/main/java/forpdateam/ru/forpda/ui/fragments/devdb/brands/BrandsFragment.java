package forpdateam.ru.forpda.ui.fragments.devdb.brands;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.devdb.Brands;
import forpdateam.ru.forpda.presentation.devdb.brands.BrandsPresenter;
import forpdateam.ru.forpda.presentation.devdb.brands.BrandsView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;

/**
 * Created by radiationx on 08.08.17.
 */

public class BrandsFragment extends RecyclerFragment implements BrandsView, BrandsAdapter.OnItemClickListener<Brands.Item> {

    public final static String ARG_CATEGORY_ID = "CATEGORY_ID";

    private BrandsAdapter adapter;

    @InjectPresenter
    BrandsPresenter presenter;

    @ProvidePresenter
    BrandsPresenter providePresenter() {
        return new BrandsPresenter(
                App.get().Di().getDevDbRepository(),
                App.get().Di().getRouter()
        );
    }

    public BrandsFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_brands));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String categoryId = getArguments().getString(ARG_CATEGORY_ID);
            if (categoryId != null) {
                presenter.initCategory(categoryId);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout.setOnRefreshListener(() -> presenter.loadBrands());
        titlesWrapper.setVisibility(View.GONE);
        toolbarSpinner.setVisibility(View.VISIBLE);

        adapter = new BrandsAdapter();
        recyclerView.setAdapter(adapter);


        toolbarSpinner.setPrompt("Category");
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.selectCategory(position);
                presenter.loadBrands();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        adapter.setOnItemClickListener(this);
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.fragment_title_device_search)
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener(item -> {
                    presenter.openSearch();
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void initCategories(@NotNull String[] categories, int position) {
        ArrayList<String> spinnerTitles = new ArrayList<>();
        for (String category : categories) {
            spinnerTitles.add(getCategoryTitle(category));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerTitles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toolbarSpinner.setAdapter(spinnerAdapter);
        toolbarSpinner.setSelection(position);
    }

    @Override
    public void showData(@NotNull Brands data) {
        setTitle(data.getCatTitle());
        adapter.clear();
        for (Map.Entry<String, ArrayList<Brands.Item>> entry : data.getLetterMap().entrySet()) {
            adapter.addSection(entry.getKey(), entry.getValue());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Brands.Item item) {
        presenter.openBrand(item);
    }

    @Override
    public boolean onItemLongClick(Brands.Item item) {
        return false;
    }

    private String getCategoryTitle(String category) {
        switch (category) {
            case BrandsPresenter.CATEGORY_PHONES:
                return App.get().getString(R.string.brands_category_phones);
            case BrandsPresenter.CATEGORY_PAD:
                return App.get().getString(R.string.brands_category_tabs);
            case BrandsPresenter.CATEGORY_EBOOK:
                return App.get().getString(R.string.brands_category_ebook);
            case BrandsPresenter.CATEGORY_SMARTWATCH:
                return App.get().getString(R.string.brands_category_smartwatch);
        }
        return null;
    }
}
