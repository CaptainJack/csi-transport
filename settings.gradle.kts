rootProject.name = "tool-csi-transport"

include(
	"client",
	"server",
	"example:client",
	"example:server"
)

arrayOf("client", "server").forEach { project(":$it").name = "${rootProject.name}-$it" }

enableFeaturePreview("GRADLE_METADATA")
