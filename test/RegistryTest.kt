import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RegistryTest {

@Test
fun secureIpTest() {
 val expected = IpInformation("192.1.134.161", 0u)
 val actual = Registry.secureIp(TargetType(ClientTargetType.CLIENT))
 assertEquals(expected.toString(), actual.toString())
}

@Test
 fun securePortTest() {
  val expected = IpInformation("127.0.0.1", 1u)
  val actual = Registry.securePort(Registry.secureIp(TargetType(ClientTargetType.SERVER)))
  assertEquals(expected.toString(), actual.toString())
 }

@Test
 fun secureIpAndPortTest() {
  val expected = IpInformation("127.0.0.1", 1u)
  val actual = Registry.SecureIpAndPort(TargetType(ClientTargetType.SERVER))
  assertEquals(expected.toString(), actual.toString())
 }

@Test
 fun freePortTest() {
  val ipInfo = Registry.SecureIpAndPort(TargetType(ClientTargetType.SERVER))
  Registry.FreePort(ipInfo)
  val expected = hashMapOf<UInt, UInt>(1u to 0u)
  val actual = DB.serversIPsAndPorts[ipInfo.ip]
  assertEquals(expected, actual)
 }

@Test
 fun freeIpAndAllPorts() {
  val ipInfo = Registry.SecureIpAndPort(TargetType(ClientTargetType.SERVER))
  Registry.FreeIpAndAllPorts(ipInfo)
 assertNull(DB.serversIPsAndPorts[ipInfo.ip])
 }
}