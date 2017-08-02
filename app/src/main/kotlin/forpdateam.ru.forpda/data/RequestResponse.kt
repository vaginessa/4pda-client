package forpdateam.ru.forpda.data

/**
 * Created by isanechek on 7/21/17.
 */
data class Request(val url: String?,
                   val category: String?,
                   val pageNumber: Int = 0)

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class Response<T>(val status: Status,
                       val data: T?,
                       val category: String?,
                       val message: String?) {

    companion object {
        const val NO_NETWORK = "no.network"
        const val LOAD_DATA_FROM_NETWORK = "load.network.data"
        const val WORKING_WITH_DATA = "working.with.data"
        const val DATA_IS_EMPTY = "data.is.empty"
        const val DATA_IS_EMPTY_NETWORK = "data.is.empty.network"
        // load more action
        const val LOAD_MORE_DATA = "load.more.date"
        const val ERROR_LOAD_MORE = "error.load.more"

        fun <T> success(data: T?, category: String?): Response<T> =
                Response(Status.SUCCESS, data, category, null)

        fun <T> error(msg: String, data: T?): Response<T> =
                Response(Status.ERROR, data, null, msg)

        fun <T> loading(data: T?, category: String?, message: String?): Response<T> =
                Response(Status.LOADING, data, category, message)
    }
}