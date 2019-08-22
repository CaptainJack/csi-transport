rootProject.name = "csi-transport"

include(
	"common",
	"client",
	"server",
	"sandbox:client-js",
	"sandbox:client-jvm",
	"sandbox:server"
)

arrayOf("common", "client", "server").forEach { project(":$it").name = "${rootProject.name}-$it" }

enableFeaturePreview("GRADLE_METADATA")
