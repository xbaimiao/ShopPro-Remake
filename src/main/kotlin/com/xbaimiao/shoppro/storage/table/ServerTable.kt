package com.xbaimiao.shoppro.storage.table

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * 全服维度的限购记录
 */
@DatabaseTable(tableName = "shoppro_server")
class ServerTable {

    @DatabaseField(generatedId = true)
    var id: Long = 0

    @DatabaseField(columnName = "item-key")
    lateinit var itemKey: String

    @DatabaseField(columnName = "data")
    lateinit var data: String

}
