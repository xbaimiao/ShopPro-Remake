package com.xbaimiao.shoppro.storage

import com.xbaimiao.easylib.database.OrmliteMysql
import org.bukkit.configuration.ConfigurationSection

/**
 * MySQL 存储
 */
class MysqlStorage(configuration: ConfigurationSection) : SqlStorage(
    OrmliteMysql(
        host = configuration.getString("host")!!,
        port = configuration.getInt("port"),
        database = configuration.getString("database")!!,
        user = configuration.getString("user")!!,
        passwd = configuration.getString("password")!!,
        ssl = configuration.getBoolean("ssl", false),
        hikariCP = true
    )
)
