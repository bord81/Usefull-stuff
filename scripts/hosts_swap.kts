import java.io.FileInputStream
import java.io.FileOutputStream

val argument = args[0]
val rootHosts = "/etc/hosts"
val all = "allow"
val blk = "block"
when (argument) {
    all -> swichHosts("hostsAllow")
    blk -> swichHosts("hostsBlock")
    else -> {
        println("invalid argument: $argument")
        println("please call with '$all' or '$blk' parameters ")
    }
}
fun swichHosts(pathToFile: String) {
    val fis = FileInputStream(pathToFile)
    val fos = FileOutputStream(rootHosts)
    var array = ByteArray(fis.available())
    fis.read(array)
    fos.write(array)
    fos.flush()
    fis.close()
    fos.close()
    println("Success!")
}

