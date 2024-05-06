plugins {
    id("java")
}

group = "de.henrik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:4.27.0-RC1")
    implementation("com.google.protobuf:protobuf-java-util:4.27.0-RC1")
    implementation("org.apache.maven:maven-model:4.0.0-alpha-13")
    implementation("org.apache.maven:maven-resolver-provider:4.0.0-alpha-13")

    implementation(project(":ProtoParser"))
}

tasks.test {
    useJUnitPlatform()
}