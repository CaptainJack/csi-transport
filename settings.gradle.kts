rootProject.name = "tool-csi-transport"

include(
	"client",
	"server",
	"sandbox:client-js",
	"sandbox:client-jvm",
	"sandbox:server"
)

arrayOf("client", "server").forEach { project(":$it").name = "${rootProject.name}-$it" }

enableFeaturePreview("GRADLE_METADATA")
