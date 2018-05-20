package forpdateam.ru.forpda.ui.navigation

import android.util.Log

class TabController {
    private var currentTag = ""
    private val tabs = mutableListOf<TabItem>()

    fun getCurrent() = findTabItem(currentTag)

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
            item.parent?.also { parent ->
                val index = parent.children.indexOf(item)
                parent.children.removeAt(index)
                parent.children.addAll(index, item.children)
                item.children.forEach { child ->
                    child.parent = parent
                }
                item.children.clear()
                currentTag = parent.tag
            }
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
            item.parent?.also { parent ->
                val index = parent.children.indexOf(item)
                parent.children.removeAt(index)
                parent.children.addAll(index, item.children)
                parent.children.add(index, newItem.also {
                    it.parent = parent
                })
                item.children.forEach { child ->
                    child.parent = parent
                }
                item.children.clear()
            }
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

    private fun printTabItems() {
        var lal = ""
        tabs.forEach {
            lal += "root->TabItem(${it.tag}, ${it.screen}, ${it.parent?.tag}, ${it.children.size})${if (currentTag == it.tag) " <-- current" else ""}\n"
            lal += printTabItemsTree(it, 1)
        }
        System.out.print(lal)
        Log.e("TabController", "tree:\n$lal")
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