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

    fun assignIP(list: MutableList<User>, ipPrefix: String) {
        var assigned = false
        for ((index, user) in list.withIndex()) {
            if (Duration.between(user.date, LocalTime.now()).toSeconds() > LIFE_TIME_SECONDS) {
                list[index] = this
                this.ip = "$ipPrefix.$index"
                assigned = true
                break
            }
        }
        if (!assigned) {
            list.add(this)
            this.ip = "$ipPrefix.${list.size - 1}"
        }
        println(list.toString())
    }
}

fun main() {
    val clientIps = mutableListOf<User>()
    val clientPrefix = "192.0.0"
    val serverIps = mutableListOf<User>()
    val serverPrefix = "127.0.0"
    while (true) {
        print("Enter your type: ")
        val type = readln()
        val user = User(type = type)
        when (user.type) {
            "c" -> user.assignIP(clientIps, clientPrefix)
            "s" -> user.assignIP(serverIps, serverPrefix)
        }
    }
}

