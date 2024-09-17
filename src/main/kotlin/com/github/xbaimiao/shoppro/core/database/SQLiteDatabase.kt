package com.github.xbaimiao.shoppro.core.database

import com.xbaimiao.easylib.database.OrmliteSQLite

class SQLiteDatabase : SqlDatabase(OrmliteSQLite("sqlite.db"))
