plugins {
    id("java")
}

group = "de.henrik"
version = "1.0"

sourceSets {
    main {
        java {
            srcDirs("out")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:4.26.1")
}