rootProject.name = "csi-transport"

include(
	"netty:common",
	"netty:client",
	"netty:server",
	
	"js:client-browser",
	
	"sandbox:server-netty",
	"sandbox:client-common",
	"sandbox:client-netty"
	
)

listOf("netty", "js").forEach { d ->
	file(d).listFiles { f: File -> f.isDirectory }!!.forEach {
		project(":$d:${it.name}").name = "${rootProject.name}-$d-${it.name}"
	}
}