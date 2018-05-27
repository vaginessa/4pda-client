package forpdateam.ru.forpda.entity.common

class AuthData {
    var userId: Int = 0
    var state = AuthState.NO_AUTH

    fun isAuth() = state == AuthState.AUTH
}