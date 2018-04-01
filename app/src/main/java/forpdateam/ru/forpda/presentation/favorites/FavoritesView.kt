package forpdateam.ru.forpda.presentation.favorites

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.favorites.FavData
import forpdateam.ru.forpda.entity.remote.favorites.FavItem

/**
 * Created by radiationx on 01.01.18.
 */

interface FavoritesView : IBaseView {
    fun onLoadFavorites(data: FavData)
    fun onShowFavorite(items: List<FavItem>)
    fun onHandleEvent(count: Int)
    fun showItemDialogMenu(item: FavItem)
    fun changeFav(action: Int, type: String, favId: Int)
    fun showSubscribeDialog(item: FavItem)
}
