package forpdateam.ru.forpda.fragments.news.main.category

import android.arch.lifecycle.LifecycleFragment
import android.graphics.Point
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.CardView
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.data.OtherNews
import forpdateam.ru.forpda.ext.loadImageFromNetwork
import forpdateam.ru.forpda.views.widgets.FixCardView

/**
 * Created by isanechek on 7/10/17.
 */
class NewsCategoryFragment : LifecycleFragment() {

    private lateinit var nested: NestedScrollView


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater?.inflate(R.layout.news_category_fragment_layout, container, false)
        nested = root?.findViewById(R.id.news_category_container) as NestedScrollView
        return root
    }

    private fun createUi() {
        val heightPx = 380 * scale()
        val lpForRoot = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx.toInt())
        val root = LinearLayout(activity)
        root.layoutParams = lpForRoot
        root.orientation = LinearLayout.VERTICAL

        val lpForContainer = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val container = FixCardView(activity)
        container.layoutParams = lpForContainer
        root.addView(container)

        val heightContent = 330 * scale()
        val lpForContent = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightContent.toInt())
        val content = LinearLayout(activity)
        content.layoutParams = lpForContent
        content.orientation = LinearLayout.VERTICAL
        root.addView(content)

        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val heightChild = size.y / 2
        val widthChild = 140 * scale()

        for (i in 0..4) {
            if (i == 0) {
                root.addView(createTitle(getString(R.string.category_title_text)))

            }
        }

        nested.addView(root)
    }

    private fun createTag(tagName: String) : View {
        val lpForText = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val tag = TextView(activity)
        tag.layoutParams = lpForText
        tag.setPadding(8, 8, 8, 8)
        tag.setTypeface(null, Typeface.BOLD)
        tag.text = tagName
        return tag
    }

    private fun createTitle(text: String) : TextView {
        val textSize = resources.getDimension(R.dimen.news_title_size_18sp)
        val title = TextView(activity)
        title.setPadding(8, 8, 8, 8)
        title.maxLines = 2
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTypeface(null, Typeface.BOLD_ITALIC)
        title.text = text
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        return title
    }

    private fun scale() = resources.displayMetrics.density

    private fun createChild(width: Int, height: Int, item: OtherNews) : View {
        val lpForRoot = ViewGroup.LayoutParams(width, height)
        val root = CardView(activity)
        root.layoutParams = lpForRoot
        val lpForContainer = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val container = LinearLayout(activity)
        container.layoutParams = lpForContainer
        container.orientation = LinearLayout.VERTICAL
        root.addView(container)

        val widthPx = 90 * scale()
        val lpForImage = ViewGroup.LayoutParams(widthPx.toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        val cover = ImageView(activity)
        cover.layoutParams = lpForImage
        cover.scaleType = ImageView.ScaleType.CENTER_CROP
        cover.loadImageFromNetwork(item.imgUrl)
        container.addView(cover)

        val title = TextView(activity)
        title.setPadding(8, 8, 8, 8)
        title.maxLines = 2
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTypeface(null, Typeface.BOLD)
        title.text = item.title
        container.addView(title)
        return root
    }

    private fun createHeader() : View {
        val lpContainer = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val container = LinearLayout(activity)
        container.layoutParams = lpContainer
        container.orientation = LinearLayout.HORIZONTAL

        val size = 50 * scale()
        val lpImage = ViewGroup.LayoutParams(size.toInt(), size.toInt())
        lpImage.height = 1
        val image = ImageView(activity)
        image.layoutParams = lpImage
        image.setPadding(8, 8, 8, 8)
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        image.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_news_bubble_chart_black_24dp))
        container.addView(image)

        val lpTextContainer = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val textContainer = LinearLayout(activity)
        textContainer.layoutParams = lpTextContainer
        textContainer.orientation = LinearLayout.VERTICAL
        container.addView(textContainer)

        val title = TextView(activity)
        title.setPadding(8, 8, 8, 8)
        title.setTypeface(null, Typeface.BOLD)
        title.text = getString(R.string.category_header_title_text)
        container.addView(title)

        return container
    }


    companion object {
        fun createInstance() : NewsCategoryFragment {
            return NewsCategoryFragment()
        }
    }

}