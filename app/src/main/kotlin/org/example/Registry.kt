package org.example
import java.time.LocalDateTime
import java.time.Duration

object Registry: GrpcApi() {
    /** Print the lists for testing */
    fun viewAll() {
        println(DB.clientsIPsAndPorts)
        println(DB.serversIPsAndPorts)
        println(DB.ipsAssignedDate)
    }

    /** Return the list of secured ips based on the type */
    private fun getList(targetType: TargetType): HashMap<String, HashMap<UInt, UInt>> =
        if (targetType.ipPortTarget == ClientTargetType.CLIENT) DB.clientsIPsAndPorts else DB.serversIPsAndPorts

    /** Return the prefix the ip based on the type */
    private fun getPrefix(targetType: TargetType): String =
        if (targetType.ipPortTarget == ClientTargetType.CLIENT) DB.clientPrefix else DB.serverPrefix

    /**
     * Increase the counter of secured ips based on the type
     * @param targetType The type of the desired ip
     * @return The counter of ips of the passed type */
    private fun getAndIncreaseCounter(targetType: TargetType): Int =
        if (targetType.ipPortTarget == ClientTargetType.CLIENT) ++DB.clientsCounter else ++DB.serversCounter

    /**
     * Determine the type of the ip (Client or Server)
     * @param ipInfo The ip of class IpInformation
     * @return The type of the ip of class TargetType*/
    private fun determineTargetType(ipInfo: IpInformation): TargetType =
        if (DB.clientsIPsAndPorts.containsKey(ipInfo.ip)) TargetType(ClientTargetType.CLIENT)
        else if (DB.serversIPsAndPorts.containsKey(ipInfo.ip))  TargetType(ClientTargetType.SERVER)
        else throw IllegalArgumentException("IP ${ipInfo.ip} not found")

    /**
     * Assign ip for the user
     * @param ip The ip to be stored
     * @param targetType The type of the ip
     * @return the IpInformation with the assigned ip*/
    private fun assignIp(ip: String, targetType: TargetType): IpInformation{
        val assignedIpInfo = IpInformation(ip, 0u)
        getList(targetType)[assignedIpInfo.ip] = hashMapOf()
        DB.ipsAssignedDate[assignedIpInfo.ip] = LocalDateTime.now().withNano(0)
        return assignedIpInfo
    }

    override fun secureIp(targetType: TargetType): IpInformation {
        for (ip in getList(targetType).keys) {
            if (DB.ipsAssignedDate[ip] == null || Duration.between(DB.ipsAssignedDate[ip], LocalDateTime.now()).toSeconds() >= DB.LIFE_TIME_SECONDS) {
                return assignIp(ip, targetType)
            }
        }
        val counter = getAndIncreaseCounter(targetType)
        val secondField = counter / (65_536) // 256*256
        val thirdField = counter%65_536 / 256
        val fourthField = counter % 256
        return assignIp("${getPrefix(targetType)}.$secondField.$thirdField.$fourthField", targetType)
    }

    override fun securePort(ipInfo: IpInformation): IpInformation {
        val targetType = determineTargetType(ipInfo)
        val newPort = getList(targetType).getValue(ipInfo.ip).entries.lastOrNull()?.value?.plus(1u) ?: 1u // get the last port
        getList(targetType)[ipInfo.ip]?.set(newPort, newPort)
        return IpInformation(ipInfo.ip, newPort)
    }

    override fun SecureIpAndPort(targetType: TargetType): IpInformation { // wrong formatting
        return securePort(secureIp(targetType))
    }

    override fun FreePort(ipInfo: IpInformation) {
        val targetType = determineTargetType(ipInfo)
        getList(targetType)[ipInfo.ip]?.set(ipInfo.port, 0u) // Set the port to 0 to free it
    }

    override fun FreeIpAndAllPorts(ipInfo: IpInformation) {
        val targetType = determineTargetType(ipInfo)
        DB.ipsAssignedDate[ipInfo.ip] = null
        getList(targetType)[ipInfo.ip] = hashMapOf()
    }

    override fun SyncSecuredIpsAndPorts(listOfIpInfo: ListOfIpInformation) {
        TODO("Not yet implemented")
    }

    override fun GetDomainName(): DomainNameInformation {
        TODO("Not yet implemented")
    }

    override fun GetAllowedIPsForClients(): ListOfAllowedIPs {
        TODO("Not yet implemented")
    }

}