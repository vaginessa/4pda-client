package forpdateam.ru.forpda.model.data.remote.api.mentions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.entity.remote.mentions.MentionItem;
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData;
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination;
import forpdateam.ru.forpda.model.data.remote.IWebClient;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsApi {
    private final static Pattern mentionsPattern = Pattern.compile("<div class=\"topic_title_post ?([^\"]*?)\"[^>]*?>([^:]*?):[^<]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>(?:([^<]*?)(?:, ([^<]*?)|))<\\/a>[\\s\\S]*?post_date[^\"]*?\"[^>]*?>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?showuser[^>]*>([\\s\\S]*?)<");

    private IWebClient webClient;

    public MentionsApi(IWebClient webClient) {
        this.webClient = webClient;
    }

    public MentionsData getMentions(int st) throws Exception {
        MentionsData data = new MentionsData();
        NetworkResponse response = webClient.get("https://4pda.ru/forum/index.php?act=mentions&st=".concat(Integer.toString(st)));
        Matcher matcher = mentionsPattern.matcher(response.getBody());
        while (matcher.find()) {
            MentionItem item = new MentionItem();
            item.setState(matcher.group(1).equals("read") ? MentionItem.STATE_READ : MentionItem.STATE_UNREAD);
            item.setType(matcher.group(2).equalsIgnoreCase("Форум") ? MentionItem.TYPE_TOPIC : MentionItem.TYPE_NEWS);
            item.setLink(matcher.group(3));
            item.setTitle(ApiUtils.fromHtml(matcher.group(4)));
            item.setDesc(ApiUtils.fromHtml(matcher.group(5)));
            item.setDate(matcher.group(6));
            item.setNick(ApiUtils.fromHtml(matcher.group(7)));
            data.addItem(item);
        }
        data.setPagination(Pagination.parseForum(response.getBody()));
        return data;
    }
}
