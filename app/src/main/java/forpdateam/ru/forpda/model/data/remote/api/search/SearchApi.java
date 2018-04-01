package forpdateam.ru.forpda.model.data.remote.api.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination;
import forpdateam.ru.forpda.entity.remote.search.SearchItem;
import forpdateam.ru.forpda.entity.remote.search.SearchResult;
import forpdateam.ru.forpda.entity.remote.search.SearchSettings;
import forpdateam.ru.forpda.model.data.remote.IWebClient;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse;

/**
 * Created by radiationx on 01.02.17.
 */

public class SearchApi {
    private final static Pattern forumTopicsPattern = Pattern.compile("<div[^>]*?data-topic=\"(\\d+)\"[^>]*?>[\\s\\S]*?(?:<font color=\"([^\"]*?)\"[^>]*?>([^<]*?)<\\/font>[\\s\\S]*?)?<a[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<span class=\"topic_desc\">(?:(?!форум)([\\s\\S]*?)<br[^>]*?>)?форум:[^<]*?<a[^>]*?showforum=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?автор:[^<]*?<a[^>]*?showuser=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?Послед[\\s\\S]*?<a[^>]*?showuser=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>(?:\\s*)?([^<]*?)<\\/div>");
    private final static Pattern forumPostsPattern = Pattern.compile("<div[^>]*?class=\"cat_name[^>]*?>[^<]*?<a[^>]*?showtopic=(\\d+)[^>]*?p=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?post_date[^>]*>([^&]*?)&[\\s\\S]*?<a[^>]*?showuser=(\\d+)[^>]*?data-av=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)<\\/?[ia][\\s\\S]*?post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div[^>]*?class=\"cat_name[^>]*?>|<div><div[^>]*?class=\"pagination|<div><\\/div><br[^>]*?><\\/form>)");
    private final static Pattern newsListPattern = Pattern.compile("<li>[^<]*?<div[^>]*?class=\"photo\"[^>]*?>[\\s\\S]*?<a[^\"]*?href=\"[^\"]*?(\\d+)\\/\"[^>]*?>[\\s\\S]*?<img[^>]*?src=\"([\\s\\S]*?)\"[^>]*?>[\\s\\S]*?class=\"date[^>]*>([\\s\\S]*?)<\\/em>[\\s\\S]*?showuser=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<h\\d[^>]*>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<p>[^<]*?<a[^>]*>([\\s\\S]*?)<\\/a>[^<]*?<\\/p>");

    private final static Pattern universalForumPosts = Pattern.compile("(?:<a name=\"entry([^\"]*?)\"[^>]*?><\\/a>|<div[^>]*?class=\"cat_name[^>]*?>[^<]*?<a[^>]*?showtopic=(\\d+)[^>]*?p=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a><\\/div>)[\\s\\S]*?<div class=\"post_header_container\"><div class=\"post_header\"><span class=\"post_date\">([^&]*?)&[^<]*?<a[^>]*?>#?([^<]*?)<\\/a>[^<]*?<\\/span>[\\s\\S]*?<font color=\"([^\"]*?)\">[^<]*?<\\/font>[\\s\\S]*?<a[^>]*?data-av=\"([^\"]*?)\"[^>]*?>([^<]*?)<[\\s\\S]*?<a[^>]*?showuser=([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[\\s\\S]*?<span[^>]*?post_user_info[^>]>(<strong[\\s\\S]*?<\\/strong>(?:<br[^>]*?>))?(?:<span[^<]*?color:([^;']*)[^>]*?>)?([\\s\\S]*?)(?:<\\/span>|)(?:  \\| [^<]*?)?<\\/span>[\\s\\S]*?(<a[^>]*?win_minus[^>]*?>[\\s\\S]*?<\\/a>|) \\([\\s\\S]*?ajaxrep[^>]*?>([^<]*?)<\\/span><\\/a>\\)[^<]*(<a[^>]*?win_add[^>]*?>[\\s\\S]*?<\\/a>|)<br[^>]*?>[^<]*?<span class=\"post_action\">(<a[^>]*?report[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?edit_post[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?delete[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?CODE=02[^>]*?>[^<]*?<\\/a>|)[^<]*[^<]*[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div[^>]*?class=\"cat_name|<div><div[^>]*?class=\"pagination|<div><\\/div><br[^>]*?><\\/form>|<div data-post|<!-- TABLE FOOTER -->|<div class=\"topic_foot_nav\">)");

    private IWebClient webClient;

    public SearchApi(IWebClient webClient) {
        this.webClient = webClient;
    }

    public SearchResult getSearch(SearchSettings settings) throws Exception {
        SearchResult result = new SearchResult();
        NetworkResponse response = webClient.get(settings.toUrl());
        Matcher matcher = null;
        SearchItem item = null;
        boolean isNews = settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first);
        boolean resultTopics = settings.getResult().equals(SearchSettings.RESULT_TOPICS.first);
        if (isNews) {
            matcher = newsListPattern.matcher(response.getBody());
            while (matcher.find()) {
                item = new SearchItem();
                item.setId(Integer.parseInt(matcher.group(1)));
                item.setImageUrl(matcher.group(2));
                item.setDate(matcher.group(3));
                item.setUserId(Integer.parseInt(matcher.group(4)));
                item.setNick(ApiUtils.fromHtml(matcher.group(5)));
                item.setTitle(ApiUtils.fromHtml(matcher.group(6)));
                item.setBody(matcher.group(7));
                result.addItem(item);
            }
        } else {
            if (resultTopics) {
                matcher = forumTopicsPattern.matcher(response.getBody());
                while (matcher.find()) {
                    item = new SearchItem();
                    item.setTopicId(Integer.parseInt(matcher.group(1)));
                    //item.setId(Integer.parseInt(matcher.group(1)));
                    item.setTitle(ApiUtils.fromHtml(matcher.group(4)));
                    item.setDesc(ApiUtils.fromHtml(matcher.group(5)));
                    item.setForumId(Integer.parseInt(matcher.group(6)));
                    item.setUserId(Integer.parseInt(matcher.group(10)));
                    item.setNick(ApiUtils.fromHtml(matcher.group(11)));
                    item.setDate(matcher.group(12));
                    result.addItem(item);
                }
            } else {
                matcher = universalForumPosts.matcher(response.getBody());
                while (matcher.find()) {
                    item = new SearchItem();
                    item.setTopicId(Integer.parseInt(matcher.group(2)));
                    item.setId(Integer.parseInt(matcher.group(3)));
                    item.setTitle(ApiUtils.fromHtml(matcher.group(4)));
                    item.setDate(matcher.group(5));
                    //item.setNumber(Integer.parseInt(matcher.group(6)));
                    item.setOnline(matcher.group(7).contains("green"));
                    String avatar = matcher.group(8);
                    if (!avatar.isEmpty()) {
                        avatar = "https://s.4pda.to/forum/uploads/".concat(avatar);
                    }
                    item.setAvatar(avatar);
                    item.setNick(ApiUtils.fromHtml(matcher.group(9)));
                    item.setUserId(Integer.parseInt(matcher.group(10)));
                    item.setCurator(matcher.group(11) != null);
                    item.setGroupColor(matcher.group(12));
                    item.setGroup(matcher.group(13));
                    item.setCanMinus(!matcher.group(14).isEmpty());
                    item.setReputation(matcher.group(15));
                    item.setCanPlus(!matcher.group(16).isEmpty());
                    item.setCanReport(!matcher.group(17).isEmpty());
                    item.setCanEdit(!matcher.group(18).isEmpty());
                    item.setCanDelete(!matcher.group(19).isEmpty());
                    item.setCanQuote(!matcher.group(20).isEmpty());
                    item.setBody(matcher.group(21));
                    result.addItem(item);
                }
            }
        }

        if (isNews) {
            result.setPagination(Pagination.parseNews(response.getBody()));
        } else {
            result.setPagination(Pagination.parseForum(response.getBody()));
        }
        result.setSettings(settings);
        return result;
    }
}
