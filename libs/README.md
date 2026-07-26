# libs 目录说明

这里放编译期需要、但不入 git 的第三方插件 API jar。

`build.gradle.kts` 用 `compileOnly(fileTree("libs"))` 引用本目录，
全部是 `compileOnly`，**不会被打进 ShopPro 的产物**，只在编译期提供类符号。

缺哪个 jar 只会影响对应挂钩的编译；跑起来之后对应插件没装，
ShopPro 会跳过那类商品并打警告，不影响其余功能。

## 需要的 jar

| 文件 | 用途 | 从哪拿 |
| --- | --- | --- |
| `craft-engine-core-26.7.4.jar` | CraftEngine 物品挂钩 | 见下方说明 |
| `craft-engine-bukkit-26.7.4.jar` | CraftEngine 物品挂钩 | 同上 |
| `Mythic-Dist-5.3.5.jar` | MythicMobs 物品挂钩 | `https://mvn.lumine.io/repository/maven-public/io/lumine/Mythic-Dist/5.3.5/Mythic-Dist-5.3.5.jar` |
| `FusangLedger-1.0.2.jar` | FusangLedger 多货币挂钩 | 本地构建 `D:\IdeaProjects\xbaimiao\FusangLedger`，产物在 `build/libs/` |

## CraftEngine 的 jar 为什么不走 Maven

`repo.momirealms.net` 对 JVM 的 TLS 指纹会被网络中间设备 reset
（curl 能通，Gradle 解析失败），所以这两个 jar 只能走本地文件。

如果本机 Gradle 缓存里有，直接从这里复制：

```
C:\Users\<用户名>\.gradle\caches\modules-2\files-2.1\net.momirealms\craft-engine-core\26.7.4\
C:\Users\<用户名>\.gradle\caches\modules-2\files-2.1\net.momirealms\craft-engine-bukkit\26.7.4\
```

否则从 CraftEngine 的发布页下载 `craft-engine-paper-plugin-<版本>.jar` 放进来也行，
里面已经包含了编译所需的类。

## 版本注意

- MythicMobs 挑 5.3.5 是因为 5.13+ 是 Java 21 字节码，而本项目用 JDK 17 编译；
  5.3.5 到 5.13.0 之间用到的 API 完全一致，运行时兼容新版本。
- NeigeItems 走 Maven（`r.irepo.space`）不用放这里，但版本锁在 1.15.113：
  1.17.24 及之后是用预发布版 Kotlin 编译的，引用会污染整个工程。
