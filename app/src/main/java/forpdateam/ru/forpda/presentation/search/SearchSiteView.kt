package forpdateam.ru.forpda.presentation.search

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings

interface SearchSiteView : IBaseView {
    fun showData(searchResult: SearchResult)
    fun fillSettingsData(settings: SearchSettings, fields: Map<String, List<String>>)
    fun onStartSearch(settings: SearchSettings)
    fun showItemDialogMenu(item: SearchItem, settings: SearchSettings)
    fun setNewsMode()
    fun setForumMode()

    fun onAddToFavorite(result: Boolean)
    fun showAddInFavDialog(page: IBaseForumPost)
}