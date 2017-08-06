package forpdateam.ru.forpda.fragments.news.main.timeline

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.data.News
import forpdateam.ru.forpda.ext.loadImageFromNetwork
import forpdateam.ru.forpda.pref.Preferences

/**
 * Created by isanechek on 7/2/17.
 */

class NewsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ItemClickListener {
        fun itemClick(itemView: View, position: Int)
        fun itemLongClick(position: Int)
        fun offlineClick(position: Int)
        fun shareClick(view: View, position: Int)
    }

    private var items = listOf<News>()
    private var clickListener: NewsAdapter.ItemClickListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is FullItem -> holder.bind(items[position], position)
            is CompatHolder -> holder.bind(items[position], position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
//        if (viewType == HEADER_TYPE) {
//        }
//
        if (viewType == COMPAT_TYPE) {
            val compatView = LayoutInflater.from(parent?.context).inflate(R.layout.news_list_item_layout, parent, false)
            return CompatHolder(compatView)
        } else if (viewType == FULL_TYPE) {
            val fullView = LayoutInflater.from(parent?.context).inflate(R.layout.news_list_full_item_layout, parent, false)
            return FullItem(fullView)
        }
        return throw IllegalArgumentException("Not Supported View Type")
    }

    override fun getItemViewType(position: Int): Int {
//        if (position == 0) {
//            return HEADER_TYPE
//        }

        if (Preferences.News().compatItem) {
            return COMPAT_TYPE
        }

        return FULL_TYPE
    }

    override fun getItemCount() = items.size

    fun insertAll(list: List<News>) {
        items = list
    }

    fun getItem(position: Int) : News {
        return items[position]
    }

    fun setClickListener(listener: NewsAdapter.ItemClickListener) {
        this.clickListener = listener
    }


    inner class FullItem(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val categoryTv: TextView = itemView?.findViewById(R.id.news_list_item_category_tv) as TextView
        val authorTv: TextView = itemView?.findViewById(R.id.news_list_item_author_tv) as TextView
        val cover: ImageView = itemView?.findViewById(R.id.news_list_item_cover) as ImageView
        val title: TextView = itemView?.findViewById(R.id.news_list_item_title_tv) as TextView
        val date: TextView = itemView?.findViewById(R.id.news_list_item_date_tv) as TextView
        val commentCount: TextView = itemView?.findViewById(R.id.news_list_item_comments_tv) as TextView
        val clickableContainer: LinearLayout = itemView?.findViewById(R.id.news_list_item_clickable_container) as LinearLayout

        fun bind(model: News, position: Int) {
            val category = model.category
            when { category.isNotEmpty() -> categoryTv.text = category }
            authorTv.text = model.author
            cover.loadImageFromNetwork(model.imgUrl)
            title.text = model.title
            date.text = model.date
            commentCount.text = model.commentsCount
            clickableContainer.setOnClickListener { clickListener?.itemClick(it, position) }
        }

    }

    inner class CompatHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val clickContainer: LinearLayout = itemView?.findViewById(R.id.news_list_item_click_container) as LinearLayout
        val title: TextView = itemView?.findViewById(R.id.news_list_item_click_title) as TextView
        val description: TextView = itemView?.findViewById(R.id.news_list_item_click_description) as TextView
        val cover: ImageView = itemView?.findViewById(R.id.news_list_item_click_cover) as ImageView
        val avatar: CircleImageView = itemView?.findViewById(R.id.news_list_item_click_user_avatar) as CircleImageView
        val username: TextView = itemView?.findViewById(R.id.news_list_item_click_username) as TextView
        val date: TextView = itemView?.findViewById(R.id.news_list_item_click_date) as TextView
        val offline: ImageButton = itemView?.findViewById(R.id.news_list_item_click_save) as ImageButton
        val share: ImageButton = itemView?.findViewById(R.id.news_list_item_click_share) as ImageButton

        fun bind(model: News, position: Int) {
            clickContainer.setOnClickListener { clickListener?.itemClick(it, position) }
            title.text = model.title
            description.text = model.description
            cover.loadImageFromNetwork(model.imgUrl)

            username.text = model.author
            date.text = model.date
            offline.setOnClickListener { clickListener?.offlineClick(position) }
            share.setOnClickListener { clickListener?.shareClick(it, position) }
        }

    }

    companion object {
        private const val FULL_TYPE = 0
        private const val COMPAT_TYPE = 1
        private const val GRID_TYPE = 2
        private const val HEADER_TYPE = 3
        private const val LOAD_MORE_TYPE = 4
    }
}