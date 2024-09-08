plugins {
    id("java")
}

group = "de.henrik"
version = "0.9.4"

repositories {
    mavenCentral()
}

tasks.buildDependents {
    dependsOn("ProtoParser:parseProto")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Implementation-Title" to "Gradle",
            "Implementation-Version" to archiveVersion,
            "Main-Class" to "Main"
        )
    }
    from(sourceSets.main.get().output)
    from(sourceSets.main.get().allSource) {
        into("src")
    }
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}



dependencies {
    compileOnly("com.google.protobuf:protobuf-java:4.27.0-RC1")
    implementation("com.google.protobuf:protobuf-java-util:4.27.0-RC1")
    implementation("org.apache.maven:maven-model:4.0.0-alpha-13")
    implementation("org.spdx:java-spdx-library:1.1.11")
    implementation("org.spdx:spdx-jackson-store:1.1.9.1")
    implementation("org.slf4j:slf4j-nop:2.0.0")
    implementation("org.metaeffekt.core:ae-security:0.115.0")
    implementation("org.cyclonedx:cyclonedx-core-java:9.0.5")

    implementation("org.fusesource.jansi:jansi:2.4.0")
//    implementation("org.jline:jline:3.21.0")

    implementation("org.hibernate.orm:hibernate-core:6.6.0.Final")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("org.xerial:sqlite-jdbc:3.36.0.1")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.6.0.Final")



    implementation(project(":ProtoParser"))
}
