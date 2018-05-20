package forpdateam.ru.forpda.ui.navigation

class TabItem {
    var tag: String = ""
    var screen: String = ""
    var parent: TabItem? = null
    val children = mutableListOf<TabItem>()
}