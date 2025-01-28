package org.example

import java.time.LocalDateTime

object DB {
    val serversIPsAndPorts = linkedMapOf<String, HashMap<UInt, UInt>>()
    val clientsIPsAndPorts = linkedMapOf<String, HashMap<UInt, UInt>>()
    val ipsAssignedDate = hashMapOf<String, LocalDateTime?>()
    val clientPrefix = "192"
    var clientsCounter = 100_000
    val serverPrefix = "127"
    var serversCounter = 0
    val LIFE_TIME_SECONDS = 2
}