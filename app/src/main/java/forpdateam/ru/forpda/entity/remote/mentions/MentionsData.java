package forpdateam.ru.forpda.entity.remote.mentions;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsData {
    private List<MentionItem> items = new ArrayList<>();
    private Pagination pagination = new Pagination();

    public List<MentionItem> getItems() {
        return items;
    }

    public void addItem(MentionItem item) {
        items.add(item);
    }

    public void setItems(List<MentionItem> items) {
        this.items = items;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
