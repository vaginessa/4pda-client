package forpdateam.ru.forpda.ui.navigation

import android.util.Log

class TabController {

    companion object {
        private const val EMPTY_TAG = ""
    }

    private var currentTag = EMPTY_TAG
    private val tabs = mutableListOf<TabItem>()

    fun getCurrent() = findTabItem(currentTag)
    fun setCurrent(tag: String) {
        val item = findTabItem(tag)
                ?: throw Exception("You want to do the impossible. You want set current tag: \"$tag\", but this tag does not exist!")
        currentTag = item.tag
    }

    fun isCurrent(tag: String?): Boolean = currentTag == tag

    fun getList(): List<TabItem> {
        val result = mutableListOf<TabItem>()
        tabs.forEach {
            result.add(it)
            result.addAll(getListTree(it))
        }
        return result
    }

    private fun getListTree(item: TabItem): List<TabItem> {
        val result = mutableListOf<TabItem>()
        item.children.forEach {
            result.add(it)
            result.addAll(getListTree(it))
        }
        return result
    }

    fun addNew(tag: String, screen: String): TabItem {
        val item = findTabItem(currentTag)
        val newItem = TabItem().also {
            it.tag = tag
            it.screen = screen
        }
        if (item != null) {
            item.children.add(newItem.apply {
                parent = item
            })
        } else {
            tabs.add(newItem)
        }
        currentTag = tag
        Log.e("TabController", "addNew t=$tag, s=$screen")
        printTabItems()
        return newItem
    }

    fun remove(tag: String, print: Boolean = true) {
        findTabItem(tag)?.also { item ->
            val parentList = item.parent?.children ?: tabs
            val index = parentList.indexOf(item)
            parentList.removeAt(index)
            parentList.addAll(index, item.children)
            item.children.forEach { child ->
                child.parent = item.parent
            }
            item.children.clear()
            currentTag = item.parent?.tag ?: getNearest(index, parentList)?.tag ?: EMPTY_TAG
            item.parent = null
        }
        Log.e("TabController", "remove t=$tag")
        if (print) {
            printTabItems()

        }
    }

    fun replace(tag: String, screen: String) {
        findTabItem(currentTag)?.also { item ->
            val newItem = TabItem().also {
                it.tag = tag
                it.screen = screen
            }
            val parentList = item.parent?.children ?: tabs
            val index = parentList.indexOf(item)
            parentList.removeAt(index)
            parentList.addAll(index, item.children)
            parentList.add(index, newItem.also {
                it.parent = item.parent
            })
            item.children.forEach { child ->
                child.parent = item.parent
            }
            item.children.clear()
            item.parent = null
        }
        currentTag = tag
        Log.e("TabController", "replace t=$tag, s=$screen")
        printTabItems()
    }

    fun backTo(screen: String): List<String> {
        val tagsRemove = mutableListOf<String>()
        findTabItem(currentTag)?.let { item ->
            var parent: TabItem? = item
            while (parent != null) {
                if (parent.screen == screen) {
                    break
                }
                tagsRemove.add(parent.tag)
                parent = parent.parent
            }
            tagsRemove.forEach { remove(it, false) }
        }
        Log.e("TabController", "backTo s=$screen")
        printTabItems()
        return tagsRemove
    }

    private fun getNearest(index: Int, list: List<TabItem>): TabItem? {
        if (list.isEmpty()) {
            return null
        }
        if (index >= 0 && index < list.size) {
            return list[index]
        }
        if (index < 0) {
            return list[0]
        }
        if (index >= list.size) {
            return list[list.size - 1]
        }
        return null
    }

    private fun findTabItem(tag: String): TabItem? {
        tabs.forEach {
            if (it.tag == tag) {
                return it
            }
            findTabItemTree(it, tag)?.also {
                return it
            }
        }
        return null
    }

    private fun findTabItemTree(tab: TabItem, tag: String): TabItem? {
        tab.children.forEach {
            if (it.tag == tag) {
                return it
            }
            findTabItemTree(it, tag)?.also {
                return it
            }
        }
        return null
    }

    fun printTabItems(logTag: String = "TabController") {
        var lal = ""
        tabs.forEach {
            lal += "root->TabItem(${it.tag}, ${it.screen}, ${it.parent?.tag}, ${it.children.size})${if (currentTag == it.tag) " <-- current" else ""}\n"
            lal += printTabItemsTree(it, 1)
        }
        System.out.print(lal)
        Log.e(logTag, "tree:\n$lal")
    }

    private fun printTabItemsTree(tab: TabItem, level: Int): String {
        var lal = ""
        tab.children.forEach {
            lal += "      "
            for (i in 1 until level) {
                lal += "+--"
            }
            lal += "+->TabItem(${it.tag}, ${it.screen}, ${it.parent?.tag}, ${it.children.size})${if (currentTag == it.tag) " <-- current" else ""}\n"
            lal += printTabItemsTree(it, level + 1)
        }
        return lal
    }
}