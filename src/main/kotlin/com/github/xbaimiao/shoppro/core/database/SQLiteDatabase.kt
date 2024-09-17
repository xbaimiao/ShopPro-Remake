package com.github.xbaimiao.shoppro.core.database

import com.github.xbaimiao.shoppro.ShopPro
import taboolib.common.io.newFile
import taboolib.common.platform.function.submit
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Table
import taboolib.module.database.getHost
import taboolib.platform.BukkitPlugin
import java.util.*

class SQLiteDatabase : SqlDatabase() {

    override val host = newFile(BukkitPlugin.getInstance().dataFolder, "sqlite.db").getHost()
    override val playerTable = Table(playerTableName, host) {
        add(itemKeyLine) {
            type(ColumnTypeSQLite.TEXT, 255)
        }
        add(itemMaterialLine) {
            type(ColumnTypeSQLite.TEXT, 255)
        }
        add(itemCustomLine) {
            type(ColumnTypeSQLite.INTEGER, 10)
        }
        add(dataLine) {
            type(ColumnTypeSQLite.TEXT, 255)
        }
        add(playerLine) {
            type(ColumnTypeSQLite.TEXT, 255)
        }
    }

    override val serverTable = Table(serverTableName, host) {
        add(itemKeyLine) {
            type(ColumnTypeSQLite.TEXT, 255)
        }
        add(itemMaterialLine) {
            type(ColumnTypeSQLite.TEXT, 255)
        }
        add(itemCustomLine) {
            type(ColumnTypeSQLite.INTEGER, 10)
        }
        add(dataLine) {
            type(ColumnTypeSQLite.TEXT, 255)
        }
    }

    override val dataSource = host.createDataSource()

    init {
        serverTable.workspace(dataSource) { createTable() }.run()
        playerTable.workspace(dataSource) { createTable() }.run()
        submit(async = true, delay = 20, period = 20) {
            val currentDay = Date().day
            if (currentDay != ShopPro.config.getInt("date")) {
                reset()
                ShopPro.config["date"] = currentDay
                ShopPro.config.saveToFile()
            }
        }
    }

}