package com.github.xbaimiao.shoppro.core.database.dao

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * @author xbaimiao
 * @date 2024/9/17
 * @email owner@xbaimiao.com
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
