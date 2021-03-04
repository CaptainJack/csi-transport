rootProject.name = "csi-transport"

include(
	"netty:common",
	"netty:client",
	"netty:server"
)

listOf("netty").forEach { d ->
	file(d).listFiles { f: File -> f.isDirectory }!!.forEach {
		project(":$d:${it.name}").name = "${rootProject.name}-$d-${it.name}"
	}
}