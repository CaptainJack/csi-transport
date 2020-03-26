import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	kotlin("jvm")
	id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation(project(":netty:csi-transport-netty-server"))
	implementation("ru.capjack.csi:csi-core-server")
	implementation("ru.capjack.tool:tool-logging")
	implementation("ch.qos.logback:logback-classic:1.2.3")
	implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

tasks.withType<ShadowJar> {
	manifest.attributes["Main-Class"] = "ru.capjack.csi.sandbox.server.netty.MainKt"
	archiveClassifier.set(null as String?)
}
