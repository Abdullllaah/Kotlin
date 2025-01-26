import java.time.LocalDateTime
import java.time.Duration

class IpInformation(val ip: String, val port: UInt){
    override fun toString(): String {
        return "(IP=${this.ip}, Port=${this.port})"
    }
}
class ListOfIpInformation(val ipInfo: List<IpInformation>)
enum class ClientTargetType {
    SERVER,
    CLIENT,
}
class TargetType(val ipPortTarget: ClientTargetType)
class DomainNameInformation(val domainName: String)
class ListOfAllowedIPs(val allowedIPs: List<String>)
//#####################################################
abstract class GrpcApi(){
    /*Secure random ip for the caller (governed by the type of the target) and return the secured ip information*/
    abstract fun secureIp(targetType:TargetType): IpInformation
    /*Secure a random port for the given ip*/
    abstract fun securePort(ipInfo:IpInformation): IpInformation
    /*Secure random ip and port for the caller (governed by the type of the target) and return the secured ip and port information*/
    abstract fun SecureIpAndPort(targetType:TargetType): IpInformation
    /*Free the given port from the given ip*/
    abstract fun FreePort(ipInfo:IpInformation): Unit
    /*Free the given ip and all ports associated with it*/
    abstract fun FreeIpAndAllPorts(ipInfo:IpInformation): Unit
    /*Sync the currently in use ips and ports from the caller to the registry*/
    abstract fun SyncSecuredIpsAndPorts(listOfIpInfo:ListOfIpInformation): Unit
    /*Gets the domain name(endpoint) for the servers services(should then be used with DNS to get the actual ip)*/
    abstract fun GetDomainName(): DomainNameInformation
    /*Gets the allowed ips for clients*/
    abstract fun GetAllowedIPsForClients(): ListOfAllowedIPs
}

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

fun main() {
    val registry = Registry

    // Test: Secure IP for a client
    println("### Securing IP for Client ###")
    val clientIp1 = registry.secureIp(TargetType(ClientTargetType.CLIENT))
    println("Secured Client IP: $clientIp1")

    val clientIp2 = registry.secureIp(TargetType(ClientTargetType.CLIENT))
    println("Secured Client IP: $clientIp2")

    // Test: Secure IP for a server
    println("\n### Securing IP for Server ###")
    val serverIp1 = registry.secureIp(TargetType(ClientTargetType.SERVER))
    println("Secured Server IP: $serverIp1")

    // Test: Secure Port for a Server IP
    println("\n### Securing 2 Ports sequentially for Server ###")
    val securedPortServer1 = registry.securePort(serverIp1)
    val securedPortServer2 = registry.securePort(serverIp1)
    println("Secured Port for Server IP: $securedPortServer1, $securedPortServer2")

    // Test: Secure IP and Port together
    println("\n### Securing IP and Port Together ###")
    val securedIpAndPort = registry.SecureIpAndPort(TargetType(ClientTargetType.SERVER))
    println("Secured IP and Port: $securedIpAndPort")

    // Test: Free Port
    println("\n### Freeing a Port ###")
    registry.FreePort(securedPortServer1)
    println("Port 1 Freed for Server IP: ${securedPortServer1.ip}")
    registry.viewAll() // View all to verify

    // Test: Free IP and All Ports
    println("\n### Freeing an IP and All Ports ###")
    registry.FreeIpAndAllPorts(serverIp1)
    println("Freed IP and All Ports for: ${serverIp1.ip}")
    registry.viewAll() // View all to verify


    // View Final State
    println("\n### Final State of DB ###")
    registry.viewAll()
}

