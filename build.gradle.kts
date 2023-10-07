val springBootVersion by extra { "2.7.16" }
val springVersion by extra { "5.3.30" }
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