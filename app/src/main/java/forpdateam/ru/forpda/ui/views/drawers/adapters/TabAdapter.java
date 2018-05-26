package forpdateam.ru.forpda.ui.views.drawers.adapters;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 02.05.17.
 */

public class TabAdapter extends BaseAdapter<TabFragment, TabAdapter.TabHolder> {
    private int color = Color.argb(48, 128, 128, 128);

    private BaseAdapter.OnItemClickListener<TabFragment> itemClickListener;
    private BaseAdapter.OnItemClickListener<TabFragment> closeClickListener;

    private List<TabFragment> tabFragments;

    public void setItemClickListener(BaseAdapter.OnItemClickListener<TabFragment> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setCloseClickListener(BaseAdapter.OnItemClickListener<TabFragment> closeClickListener) {
        this.closeClickListener = closeClickListener;
    }

    public void setItems(List<TabFragment> tabs) {
        tabFragments = tabs;
        notifyDataSetChanged();
    }

    public TabFragment getItem(int position) {
        //todo fix it
        //return TabManager.get().get(position);
        return tabFragments.get(position);
    }

    @Override
    public int getItemCount() {
        //todo fix it
        //return TabManager.get().getSize();
        return tabFragments.size();
    }

    @Override
    public TabHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.drawer_tab_item);
        return new TabHolder(v);
    }

    @Override
    public void onBindViewHolder(TabHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class TabHolder extends BaseViewHolder<TabFragment> implements View.OnClickListener {
        public TextView text;
        public ImageView close;

        TabHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.drawer_item_title);
            close = (ImageView) v.findViewById(R.id.drawer_item_close);

            v.setOnClickListener(this);
            close.setOnClickListener(v1 -> {
                if (closeClickListener != null) {
                    closeClickListener.onItemClick(getItem(getLayoutPosition()));
                }
            });
        }

        @Override
        public void bind(TabFragment item, int position) {
            //todo fix it
            /*if (position == TabManager.getActiveIndex())
                itemView.setBackgroundColor(color);
            else
                itemView.setBackgroundColor(Color.TRANSPARENT);*/

            itemView.setBackgroundColor(Color.TRANSPARENT);
            text.setText(item.getTabTitle());
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()));
            }
        }
    }
}