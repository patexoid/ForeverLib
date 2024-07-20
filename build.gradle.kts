val springBootVersion by extra { "3.3.2" }
val springVersion by extra { "6.1.11" }
val springSecurityVersion by extra { "6.3.1" }
val lombokVersion  by extra { "1.18.34" }
subprojects {

    repositories {
        mavenCentral()
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/patexoid/repo")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }



}