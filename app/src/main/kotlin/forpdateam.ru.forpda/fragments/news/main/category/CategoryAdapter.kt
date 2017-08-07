package forpdateam.ru.forpda.fragments.news.main.category

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.data.Category
import forpdateam.ru.forpda.views.widgets.TextViewLabel

/**
 * Created by isanechek on 8/7/17.
 */
class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var items = mutableListOf<Category>()
    private var clickListener: ClickItemListener? = null

    override fun onBindViewHolder(holder: CategoryViewHolder?, position: Int) {
        holder?.bind(items[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.news_category_item_layout, parent, false)
        return CategoryViewHolder(view)
    }

    fun insertItems(list: List<Category>) {
        items.addAll(list)
    }

    fun setOnClickListener(listener: ClickItemListener) {
        this.clickListener = listener
    }

    override fun getItemCount(): Int = items.size

    inner class CategoryViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView?.findViewById(R.id.news_category_title) as TextView
        val container: LinearLayout = itemView?.findViewById(R.id.news_category_container) as LinearLayout

        fun bind(model: Category, position: Int) {
            title.setText(model.title)
            for ((title1, key) in model.subItems) {
                val tv = TextViewLabel(itemView?.context)
                tv.setText(title1)
                tv.setAllCaps(false)
                tv.setOnClickListener { clickListener?.clickItem(key) }
                container.addView(tv)
            }
        }

    }

    interface ClickItemListener {
        fun clickItem(category: String)

    }
}