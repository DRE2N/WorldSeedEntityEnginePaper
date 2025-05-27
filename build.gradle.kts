import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    `maven-publish`
    signing
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.github.goooler.shadow") version "8.1.5"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

publishing {
    publications.create<MavenPublication>("maven") {
        groupId = "net.worldseed.multipart"
        artifactId = "WorldSeedEntityEngine"
        version = "12.1"

        from(components["java"])
    }

    repositories {
        maven {
            name = "WorldSeed"
            url = uri("https://reposilite.worldseed.online/public")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")

    implementation("commons-io:commons-io:2.11.0")
    implementation("org.zeroturnaround:zt-zip:1.8")

    implementation("javax.json:javax.json-api:1.1.4")
    implementation("org.glassfish:javax.json:1.1.4")

    implementation("dev.hollowcube:mql:1.0.1")
}

tasks {
    shadowJar {
        dependencies {
            include(dependency("org.zeroturnaround:zt-zip:1.8"))
            include(dependency("javax.json:javax.json-api:1.1.4"))
            include(dependency("org.glassfish:javax.json:1.1.4"))
            include(dependency("dev.hollowcube:mql:1.0.1"))
            include(dependency("commons-io:commons-io:2.11.0"))
        }
    }
    bukkit {
        name = "Daedalus"
        main = "net.worldseed.plugin.DaedalusPlugin"
        apiVersion = "1.21"
        authors = listOf("Malfrador")
        load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
        commands {
            register("daedalus") {
                description = "Main command for Daedalus"
                aliases = listOf("dd", "dae")
                permission = "daedalus.cmd"
                usage = "/daedalus <model id>"
            }
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}