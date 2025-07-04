plugins {
    id("java")
    id("maven-publish")
}

group = "io.github.studentrentalsystem"
version = "1.0.0"

repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/StudentRentalSystem/llmdataparser")
        credentials {
            username = "x-access-token"
            password = System.getenv("CLIENT_TOKEN")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("org.seleniumhq.selenium:selenium-java:4.20.0")
    implementation(platform("org.mongodb:mongodb-driver-bom:5.4.0"))
    implementation("org.mongodb:mongodb-driver-sync")
    implementation("org.json:json:20231013")
    implementation("io.github.studentrentalsystem:llmdataparser:1.0.2")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "io.github.studentrentalsystem"
            artifactId = "facebook-rental-crawler"
            version = "1.0.0"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/StudentRentalSystem/FacebookRentalCrawler")
            credentials {
                username = System.getenv("PUBLISH_USERNAME")
                password = System.getenv("PUBLISH_TOKEN")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}