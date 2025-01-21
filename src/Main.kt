import java.time.LocalTime
import java.time.Duration

data class User (val name: String = "aa",
                 val type: String,
                 val date: LocalTime = LocalTime.now().withNano(0),
                 var ip: String = "")

fun assignIp(list: MutableList<User>, user: User, ip: String) {
    var assigned = false
    for ((index, preUser) in list.withIndex()) {
        if (Duration.between(preUser.date, LocalTime.now()).toSeconds() > 10) {
            list[index] = user
            user.ip = ip + index
            assigned = true
            break
        }
    }
    if (!assigned) {
        list.add(user)
        user.ip = ip + (list.size - 1)
    }
    println(list.toString())
}

fun main() {
    val clientIps = mutableListOf<User>()
    val serverIps = mutableListOf<User>()
    while (true) {
        print("Enter your type: ")
        val type = readln()
        val user = User(type = type)
        when (user.type) {
            "c" -> assignIp(clientIps, user, "192.0.0.")
            "s" -> assignIp(serverIps, user, "127.0.0.")
        }
    }
}

