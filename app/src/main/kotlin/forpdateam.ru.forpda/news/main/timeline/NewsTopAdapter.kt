package forpdateam.ru.forpda.news.main.timeline

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.data.OtherNews
import forpdateam.ru.forpda.ext.loadImageFromNetwork
import forpdateam.ru.forpda.views.widgets.FixCardView

/**
 * Created by isanechek on 7/8/17.
 */
class NewsTopAdapter : RecyclerView.Adapter<NewsTopAdapter.NewsTopHolder>() {

    private var items: List<OtherNews> = listOf()

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: NewsTopHolder?, position: Int) {
        holder?.bind(items[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NewsTopHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.news_top_item_layout, parent, false)
        return NewsTopHolder(view)
    }

    interface ItemClickListener {
        fun itemClick(itemView: View, position: Int)
        fun itemLongClick(position: Int)
    }

    private var clickListener: ItemClickListener? = null

    fun setOnClickItemListener(listener: ItemClickListener) {
        this.clickListener = listener
    }

    fun insert(list: List<OtherNews>) {
        items = list
    }

    inner class NewsTopHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        private val root: FixCardView = itemView?.findViewById(R.id.top_news_conteiner) as FixCardView
        private val cover: ImageView = itemView?.findViewById(R.id.top_news_cover) as ImageView
        private val title: TextView = itemView?.findViewById(R.id.top_news_title) as TextView
        private val count: TextView = itemView?.findViewById(R.id.top_news_comments_count) as TextView

        fun bind(model: OtherNews, position: Int) {
            val imgUrl = model.imgUrl
            if (imgUrl.isNotEmpty()) { cover.loadImageFromNetwork(model.imgUrl) }
            title.text = model.title
            count.text = model.commentsCount
            root.setOnClickListener { clickListener?.itemClick(it, position) }
        }
    }
}