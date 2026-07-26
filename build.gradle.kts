import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktVersion: String by project
val easylibVersion: String by project
val neigeItemsVersion: String by project
val spigotApiVersion: String by project

plugins {
    java
    id("com.gradleup.shadow")
    id("com.xbaimiao.easylib")
    kotlin("jvm")
}

// 中文写在这里而不是 gradle.properties
// Properties 规范按 ISO-8859-1 读取 .properties, UTF-8 存的中文会被读成乱码
// 最终在 plugin.yml 里被 SnakeYAML 输出成 !!binary base64
description = "支持 CraftEngine 的多货币限购商店"

// shadowJar 的 relocate 目标必须用这个常量而不是 project.group
// easylib-gradle-plugin 1.3.2 的 generatePluginYml 有个副作用
// 会把 project.group 覆盖成字面量 "build", 而 shadowJar 配置求值晚于该副作用
// 直接用 ${project.group} 会把类 relocate 到 build/shadow/ 目录下
val shadowBasePackage = "com.xbaimiao.shoppro"

easylib {
    env {
        mainClassName = "$shadowBasePackage.ShopPro"
        pluginName = "ShopPro"
        kotlinVersion = ktVersion
        authors.add("xbaimiao")
        // 1.18.2 起支持, 高版本靠 Bukkit 向后兼容运行
        apiVersion = "1.18"
        softDepend.add("PlaceholderAPI")
        softDepend.add("Vault")
        softDepend.add("PlayerPoints")
        softDepend.add("CraftEngine")
        softDepend.add("MythicMobs")
        softDepend.add("NeigeItems")
        softDepend.add("FusangLedger")
        commands.add(com.xbaimiao.easylib.Env.Command("shoppro", "ShopPro 主命令", listOf("shop")))
    }
    version = easylibVersion

    library("com.zaxxer:HikariCP:4.0.3", true)

    library("com.j256.ormlite:ormlite-core:6.1", true)
    library("com.j256.ormlite:ormlite-jdbc:6.1", true)

    relocate("com.j256.ormlite", "$shadowBasePackage.shadow.ormlite", true)
    relocate("com.zaxxer.hikari", "$shadowBasePackage.shadow.hikari", true)
    relocate("com.xbaimiao.easylib", "$shadowBasePackage.easylib", false)
    relocate("kotlin", "$shadowBasePackage.shadow.kotlin", true)
    relocate("kotlinx", "$shadowBasePackage.shadow.kotlinx", true)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    // NeigeItems
    maven("https://r.irepo.space/maven/")
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    compileOnly(kotlin("stdlib-jdk8"))
    // 按 1.18.2 编译, 靠 Bukkit 向后兼容跑到 26.x
    // paper-api 26.x 的 Gradle 元数据要求 jvm.version=25, 会锁死 JDK 版本, 因此不用它
    compileOnly("org.spigotmc:spigot-api:$spigotApiVersion")
    // 锁在 1.15.113: 1.17.24 及之后是用预发布版 Kotlin 2.0.0 编的
    // 引用它会要求整个工程开 -Xskip-prerelease-check, 那会把 ShopPro 自己的类
    // 也标成 pre-release, 导致别的插件挂钩 ShopPro API 时一并报错
    // 1.15.113 的 isNiItem/getItemStack/ItemInfo API 和新版一致, 运行时兼容新版 NI
    compileOnly("pers.neige.neigeitems:NeigeItems:$neigeItemsVersion")
    // CraftEngine / MythicMobs / FusangLedger 走 libs/ 本地 jar, 不入 git
    // 需要哪些 jar 和从哪拿见 libs/README.md
    // (CraftEngine 走本地是因为 repo.momirealms.net 对 JVM 的 TLS 指纹
    //  会被网络中间设备 reset, curl 正常但 Gradle 解析失败)
    compileOnly(fileTree("libs"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    processResources {
        outputs.upToDateWhen { false }
    }
    shadowJar {
        // easylib-gradle-plugin 只给 jar 挂了这个依赖, 没给 shadowJar 挂
        // Shadow 9.x 对隐式任务依赖校验更严, 不显式声明会构建失败
        dependsOn("generatePluginYml")
        dependencies {
            easylib.library.forEach {
                if (it.cloud) {
                    exclude(dependency(it.id))
                }
            }
            exclude(dependency("org.slf4j:"))
            exclude(dependency("org.jetbrains:annotations:"))
            exclude(dependency("com.google.code.gson:gson:"))
            exclude(dependency("org.jetbrains.kotlin:"))
            exclude(dependency("org.jetbrains.kotlinx:"))
        }
        archiveClassifier.set("")
        easylib.relocate.forEach {
            relocate(it.pattern, it.replacement)
        }
        minimize()
    }
}
