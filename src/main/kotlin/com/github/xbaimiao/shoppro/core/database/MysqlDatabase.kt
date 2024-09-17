package com.github.xbaimiao.shoppro.core.database

import com.github.xbaimiao.shoppro.ShopPro
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.util.*

class MysqlDatabase(configuration: Configuration) : SqlDatabase() {

    override val host = configuration.getHost("mysql")

    override val playerTable = Table(playerTableName, host) {
        add(itemKeyLine) {
            type(ColumnTypeSQL.VARCHAR, 255)
        }
        add(itemMaterialLine) {
            type(ColumnTypeSQL.VARCHAR, 255)
        }
        add(itemCustomLine) {
            type(ColumnTypeSQL.INT, 10)
        }
        add(dataLine) {
            type(ColumnTypeSQL.VARCHAR, 255)
        }
        add(playerLine) {
            type(ColumnTypeSQL.VARCHAR, 255)
        }
    }

    override val serverTable = Table(serverTableName, host) {
        add(itemKeyLine) {
            type(ColumnTypeSQL.VARCHAR, 255)
        }
        add(itemMaterialLine) {
            type(ColumnTypeSQL.VARCHAR, 255)
        }
        add(itemCustomLine) {
            type(ColumnTypeSQL.INT, 10)
        }
        add(dataLine) {
            type(ColumnTypeSQL.VARCHAR, 255)
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