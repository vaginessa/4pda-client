package forpdateam.ru.forpda.news.main.timeline

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.data.News
import forpdateam.ru.forpda.ext.loadImageFromNetwork

/**
 * Created by isanechek on 7/2/17.
 */

class NewsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ItemClickListener {
        fun itemClick(itemView: View, position: Int)
        fun itemLongClick(position: Int)
    }

    private var items = listOf<News>()
    private var clickListener: ItemClickListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is FullItem) {
            holder.bind(items[position], position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
//        if (viewType == HEADER_TYPE) {
//        }
//
//        if (Preferences.News().compatItem) {
//
//        }
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.news_list_full_item_layout, parent, false)
        return FullItem(view)
    }

    override fun getItemViewType(position: Int): Int {
//        if (position == 0) {
//            return HEADER_TYPE
//        }
        return FULL_TYPE
    }

    override fun getItemCount() = items.size

    fun insertAll(list: List<News>) {
        items = list
    }

    fun getItem(position: Int) : News {
        return items[position]
    }

    fun setClickListener(listener: ItemClickListener) {
        this.clickListener = listener
    }


    inner class FullItem(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val categoryTv: TextView = itemView?.findViewById(R.id.news_list_item_category_tv) as TextView
        val authorTv: TextView = itemView?.findViewById(R.id.news_list_item_author_tv) as TextView
        val cover: ImageView = itemView?.findViewById(R.id.news_list_item_author_pic) as ImageView
        val title: TextView = itemView?.findViewById(R.id.news_list_item_title_tv) as TextView
        val date: TextView = itemView?.findViewById(R.id.news_list_item_date_tv) as TextView
        val commentCount: TextView = itemView?.findViewById(R.id.news_list_item_comments_tv) as TextView

        fun bind(model: News, position: Int) {
            val category = model.category
            when { category.isNotEmpty() -> categoryTv.text = category }
            authorTv.text = model.author
            cover.loadImageFromNetwork(model.imgUrl)
            title.text = model.title
            title.setOnClickListener { clickListener?.itemClick(it, position) }
            date.text = model.date
            commentCount.text = model.commentsCount
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