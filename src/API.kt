import java.time.Duration
import java.time.LocalTime

class IpInformation(val ip: String, val port: UInt)
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
    val clientsIPsAndPorts = hashMapOf<String, MutableList<UInt>>()
    val serversIPsAndPorts = hashMapOf<String, MutableList<UInt>>()
    val clientPrefix = "192.0.0"
    var clientsCounter = 0
    val serverPrefix = "127.0.0"
    var serversCounter = 0
    val LIFE_TIME_SECONDS = 10
}

object Regestiry: GrpcApi() {
    override fun secureIp(targetType: TargetType): IpInformation {
        return when (targetType.ipPortTarget) {
            ClientTargetType.CLIENT -> {
                val IPinfo = IpInformation("${DB.clientPrefix}.${++DB.clientsCounter}", 0u)
                DB.clientsIPsAndPorts.getOrPut(IPinfo.ip) { mutableListOf() }.add(IPinfo.port)
                IPinfo
            }
            ClientTargetType.SERVER -> {
                val IPinfo = IpInformation("${DB.serverPrefix}.${++DB.serversCounter}", 0u)
                DB.serversIPsAndPorts.getOrPut(IPinfo.ip) { mutableListOf() }.add(IPinfo.port)
                IPinfo
            }
        }
    }

    override fun securePort(ipInfo: IpInformation): IpInformation {
        return when {
            DB.clientsIPsAndPorts.containsKey(ipInfo.ip) -> {
                val size = DB.clientsIPsAndPorts[ipInfo.ip]?.size?.toUInt() ?: 0u
                DB.clientsIPsAndPorts[ipInfo.ip]?.add(size)
                IpInformation(ipInfo.ip, size)
            }
            DB.serversIPsAndPorts.containsKey(ipInfo.ip) -> {
                val size = DB.serversIPsAndPorts[ipInfo.ip]?.size?.toUInt() ?: 0u
                DB.serversIPsAndPorts[ipInfo.ip]?.add(size)
                IpInformation(ipInfo.ip, size)
            }
            else -> ipInfo
        }
    }

    override fun SecureIpAndPort(targetType: TargetType): IpInformation {
        return securePort(secureIp(targetType))
    }

    override fun FreePort(ipInfo: IpInformation) {
        when {
            DB.clientsIPsAndPorts.containsKey(ipInfo.ip) ->
                DB.clientsIPsAndPorts[ipInfo.ip]?.set(ipInfo.port.toInt(), 0u)
            DB.serversIPsAndPorts.containsKey(ipInfo.ip) ->
                DB.clientsIPsAndPorts[ipInfo.ip]?.set(ipInfo.port.toInt(), 0u)
        }
    }

    override fun FreeIpAndAllPorts(ipInfo: IpInformation) {
        when {
            DB.clientsIPsAndPorts.containsKey(ipInfo.ip) ->
                DB.clientsIPsAndPorts.remove(ipInfo.ip)
            DB.serversIPsAndPorts.containsKey(ipInfo.ip) ->
                DB.clientsIPsAndPorts.remove(ipInfo.ip)
        }
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