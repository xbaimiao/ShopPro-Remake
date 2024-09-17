package com.github.xbaimiao.shoppro.core.database

import com.xbaimiao.easylib.database.OrmliteMysql
import org.bukkit.configuration.ConfigurationSection

class MysqlDatabase(configuration: ConfigurationSection) : SqlDatabase(
    OrmliteMysql(
        host = configuration.getString("host")!!,
        port = configuration.getInt("port"),
        database = configuration.getString("database")!!,
        user = configuration.getString("user")!!,
        passwd = configuration.getString("password")!!,
        ssl = false,
        hikariCP = true
    )
)
