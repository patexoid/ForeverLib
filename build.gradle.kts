val springBootVersion by extra { "4.1.0" }
val springVersion by extra { "7.0.8" }
val springSecurityVersion by extra { "7.1.0" }
val lombokVersion  by extra { "1.18.46" }
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
        options.compilerArgs.add("-parameters")
    }



}