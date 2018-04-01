package forpdateam.ru.forpda.model.data.remote.api.reputation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination;
import forpdateam.ru.forpda.entity.remote.reputation.RepData;
import forpdateam.ru.forpda.entity.remote.reputation.RepItem;
import forpdateam.ru.forpda.model.data.remote.IWebClient;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest;
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse;

/**
 * Created by radiationx on 20.03.17.
 */

public class ReputationApi {
    public final static String MODE_TO = "to";
    public final static String MODE_FROM = "from";
    public final static String SORT_ASC = "asc";
    public final static String SORT_DESC = "desc";
    private final static Pattern listPattern = Pattern.compile("<tr>[^<]*?<td[^>]*?><strong><a [^>]*?showuser=(\\d+)[^\"]*\">([\\s\\S]*?)<\\/a><\\/strong><\\/td>[^<]*?<td[^>]*?>(?:<strong><a href=\"([^\"]*?)\">([\\s\\S]*?)<\\/a><\\/strong>)?[^<]*?<\\/td>[^<]*?<td[^>]*?>([\\s\\S]*?)<\\/td>[^<]*?<td[^>]*?><img[^>]*?src=\"([^\"]*?)\"[^>]*?><\\/td>[^<]*?<td[^>]*?>([\\s\\S]*?)<\\/td>[^<]*?<\\/tr>");
    private final static Pattern infoPattern = Pattern.compile("<div class=\"maintitle\">[\\s\\S]*?<a href=\"[^\"]*?showuser=(\\d+)\"[^>]*?>([\\s\\S]*?)<\\/a>[^\\[]*?\\[\\+(\\d+)?\\/\\-(\\d+)?\\]");

    private IWebClient webClient;

    public ReputationApi(IWebClient webClient) {
        this.webClient = webClient;
    }

    public RepData getReputation(RepData data) throws Exception {
        if (data == null) return null;
        NetworkResponse response = webClient.get("https://4pda.ru/forum/index.php?act=rep&view=history&mid=" + data.getId() + "&mode=" + data.getMode() + "&order=" + data.getSort() + "&st=" + data.getPagination().getSt());
        Matcher matcher = infoPattern.matcher(response.getBody());
        if (matcher.find()) {
            data.setId(Integer.parseInt(matcher.group(1)));
            data.setNick(ApiUtils.fromHtml(matcher.group(2)));
            if (matcher.group(3) != null) {
                data.setPositive(Integer.parseInt(matcher.group(3)));
            }
            if (matcher.group(4) != null) {
                data.setNegative(Integer.parseInt(matcher.group(4)));
            }
        }


        matcher = listPattern.matcher(response.getBody());
        String temp;
        data.getItems().clear();
        while (matcher.find()) {
            RepItem item = new RepItem();
            item.setUserId(Integer.parseInt(matcher.group(1)));
            item.setUserNick(ApiUtils.fromHtml(matcher.group(2)));
            temp = matcher.group(3);
            if (temp != null) {
                item.setSourceUrl(temp);
                item.setSourceTitle(ApiUtils.fromHtml(matcher.group(4)));
            }
            item.setTitle(ApiUtils.fromHtml(matcher.group(5)));
            item.setImage(matcher.group(6));
            item.setDate(matcher.group(7));
            data.addItem(item);
        }
        data.setPagination(Pagination.parseForum(data.getPagination(), response.getBody()));
        return data;
    }

    public Boolean editReputation(int postId, int userId, boolean type, String message) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php")
                .formHeader("act", "rep")
                .formHeader("mid", Integer.toString(userId))
                .formHeader("type", type ? "add" : "minus")
                .formHeader("message", message);
        if (postId > 0) {
            builder.formHeader("p", Integer.toString(postId));
        }
        webClient.request(builder.build());
        return true;
    }

    public static RepData fromUrl(String url) {
        return fromUrl(new RepData(), url);
    }

    public static RepData fromUrl(RepData data, String url) {
        Matcher matcher = Pattern.compile("st=(\\d+)").matcher(url);
        if (matcher.find()) {
            data.getPagination().setSt(Integer.parseInt(matcher.group(1)));
        }
        matcher = Pattern.compile("mid=(\\d+)").matcher(url);
        if (matcher.find())
            data.setId(Integer.parseInt(matcher.group(1)));
        matcher = Pattern.compile("mode=([^&]+)").matcher(url);
        if (matcher.find()) {
            switch (matcher.group(1)) {
                case MODE_FROM:
                    data.setMode(MODE_FROM);
                    break;
                case MODE_TO:
                    data.setMode(MODE_TO);
                    break;
            }
        }

        matcher = Pattern.compile("order=([^&]+)").matcher(url);
        if (matcher.find()) {
            switch (matcher.group(1)) {
                case SORT_ASC:
                    data.setMode(SORT_ASC);
                    break;
                case SORT_DESC:
                    data.setMode(SORT_DESC);
                    break;
            }
        }
        return data;
    }
}
