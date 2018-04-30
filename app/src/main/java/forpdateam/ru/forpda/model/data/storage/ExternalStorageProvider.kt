package forpdateam.ru.forpda.model.data.storage

interface ExternalStorageProvider {
    fun saveTextDefault(text: String, fileName: String): String
    fun saveText(text: String, fileName: String, path: String): String
}