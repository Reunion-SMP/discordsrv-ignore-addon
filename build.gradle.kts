plugins {
	java
	alias(libs.plugins.shadow)
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
	compileOnly(libs.paper.api)
	compileOnly(libs.discordsrv.api)
	compileOnly(libs.adventure.api)
	implementation(libs.jedis)
}

val pat = "-"
fun String.toCase(sep: String) = replace(pat, sep).lowercase()
fun String.toClassName() = split(pat).joinToString("", transform={ it.lowercase().capitalize() })

val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val pluginDescription: String by project

val pluginId = pluginName.toCase("-")

group = pluginGroup
version = pluginVersion
description = pluginDescription
base.archivesName = pluginId

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
	}
}

tasks {
	processResources {
		filesMatching("plugin.yml") {
			expand(
				"plugin" to mapOf(
					"name" to pluginName,
					"description" to pluginDescription,
					"version" to pluginVersion,
					"id" to pluginId,
					"pkg" to "${pluginGroup}.${pluginName.toCase("_")}",
					"class" to pluginName.toClassName(),
				),
				"versions" to mapOf(
					"api" to libs.versions.paper.api.get().replace("\\.\\d+-.*".toRegex(), ""),
				)
			)
		}
	}

	jar {
		finalizedBy(shadowJar)
	}

	shadowJar {
		minimize()
		relocate("redis.clients.jedis", "${pluginGroup}.deps.jedis")

		exclude("META-INF/maven/**")
		exclude("META-INF/proguard/**")

		from("LICENSE") {
			rename {
				"LICENSE-${pluginId}"
			}
		}
	}

	withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
	}
}
