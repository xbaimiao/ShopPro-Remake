pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.xbaimiao.com/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
    //kotlin 版本
    val ktVersion: String by settings
    //shadowJar 版本
    val shadowJarVersion: String by settings
    val easylibPluginVersion: String by settings
    plugins {
        kotlin("jvm") version ktVersion
        id("com.gradleup.shadow") version shadowJarVersion
        id("com.xbaimiao.easylib") version easylibPluginVersion
    }
}
rootProject.name = "ShopPro"
