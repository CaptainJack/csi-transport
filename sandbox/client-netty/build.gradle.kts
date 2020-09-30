import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	kotlin("jvm")
	id("com.github.johnrengelman.shadow") version "6.0.0"
}

dependencies {
	implementation(project(":netty:csi-transport-netty-client"))
	implementation(project(":sandbox:client-common"))
	
	implementation("ch.qos.logback:logback-classic:1.2.3")
	implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

tasks.withType<ShadowJar> {
	manifest.attributes["Main-Class"] = "ru.capjack.csi.sandbox.client.common.MainKt"
	archiveClassifier.set(null as String?)
}
