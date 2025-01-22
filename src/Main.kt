import java.time.LocalTime
import java.time.Duration

data class User (var id: Int = 0,
                 val type: String,
                 val date: LocalTime = LocalTime.now(),
                 var ip: String = ""){
    companion object{
        var idCounter = 0
        val LIFE_TIME_SECONDS = 10
    }
    init {
        id = ++idCounter
    }
}

class Users(val clientList: MutableList<User> = mutableListOf(),
            val serverList: MutableList<User> = mutableListOf(),
            val clientPrefix: String = "192.0.0",
            val serverPrefix: String = "127.0.0") {
    fun assignIP(user: User) {
        val (list, ipPrefix) = if (user.type == "client") Pair(clientList, clientPrefix) else Pair(serverList, serverPrefix)
        var assigned = false
        for ((index, oldUser) in list.withIndex()) {
            if (Duration.between(oldUser.date, LocalTime.now()).toSeconds() > User.LIFE_TIME_SECONDS) {
                list[index] = user
                user.ip = "$ipPrefix.$index"
                assigned = true
                break
            }
        }
        if (!assigned) {
            list.add(user)
            user.ip = "$ipPrefix.${list.size - 1}"
        }
        println(list.toString())
    }
}

fun main() {
    val users = Users()
    while (true) {
        print("Enter your type: ")
        val type = readln()
        val user = User(type = type)
        when (user.type) {
            "client", "server" -> users.assignIP(user)
            else -> println("Invalid input")
        }
    }
}

