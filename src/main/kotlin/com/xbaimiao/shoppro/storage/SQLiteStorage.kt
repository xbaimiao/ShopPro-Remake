package com.xbaimiao.shoppro.storage

import com.xbaimiao.easylib.database.OrmliteSQLite

/**
 * SQLite 存储 单服使用
 */
class SQLiteStorage : SqlStorage(OrmliteSQLite("sqlite.db"))
