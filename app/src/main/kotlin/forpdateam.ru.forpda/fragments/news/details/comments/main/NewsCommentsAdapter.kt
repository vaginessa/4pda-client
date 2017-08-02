package forpdateam.ru.forpda.fragments.news.details.comments.main

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import forpdateam.ru.forpda.data.Comment

/**
 * Created by isanechek on 8/1/17.
 */
class NewsCommentsAdapter(headerView: View) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val comments = ArrayList<Comment>()
    private val header: View = headerView

    private val TYPE_HEADER = 0
    private val TYPE_NO_COMMENTS = 1
    private val TYPE_COMMENT = 2
    private val TYPE_COMMENT_REPLY = 3
    private val TYPE_FOOTER = 4

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (getItemViewType(position)) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_HEADER -> HeaderHolder(parent)
        TYPE_COMMENT -> CommentHolder(parent)
        else -> throw IllegalArgumentException("Not found view type")
    }

    override fun getItemCount(): Int = comments.size


    // holders
    class CommentHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)

    class HeaderHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)

    class NoCommentsHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}