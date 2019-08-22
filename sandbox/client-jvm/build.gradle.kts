import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	kotlin("jvm")
	id("com.github.johnrengelman.shadow") version "5.1.0"
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation(project(":csi-transport-client"))
	implementation("ru.capjack.csi:csi-core-client")
	implementation("ru.capjack.tool:tool-utils")
	implementation("ru.capjack.tool:tool-logging")
	implementation("ch.qos.logback:logback-classic:1.2.3")
	implementation("io.netty:netty-transport")
}

tasks.withType<ShadowJar> {
	manifest.attributes["Main-Class"] = "ru.capjack.tool.csi.transport.sandbox.client.MainKt"
	
	archiveBaseName.set("csi-sandbox-client")
	archiveClassifier.set(null as String?)
	archiveVersion.set(null as String?)
}