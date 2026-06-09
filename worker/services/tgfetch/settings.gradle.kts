rootProject.name = "tgfetch"

dependencyResolutionManagement {
    repositories {
        mavenCentral()

        maven {
            name = "mchvRepositoryMchv"
            url = uri("https://mvn.mchv.eu/mchv")
        }

        maven {
            url = uri("https://jitpack.io")
        }
    }
}
