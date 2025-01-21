fun allocateRooms(customers: Array<IntArray>): IntArray {
    var rooms = IntArray(customers.size)
//    customers.
    for ((i, c) in customers.withIndex()) {
        if (i == 0)
            rooms[0] = 1
        else {
            for ((ii, cc) in customers.slice(0..i - 1).withIndex()) {
                if (c[0] > cc[1]) {
                    for (iii in rooms.withIndex().filter { it.value == rooms[ii] }.map { it.index }) {
                        var occupied = false
                        if (c[0] <= cc[1]) break
                        rooms[i] = rooms[ii]
                    }
                }
            }
        }
    }
    return rooms
}

fun main() {
    println(allocateRooms(arrayOf(intArrayOf(1, 5),   intArrayOf(2, 4),   intArrayOf(6, 8),  intArrayOf(7, 7))).contentToString())
}
