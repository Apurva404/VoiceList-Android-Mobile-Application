package com.apurva.voiceList.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.apurva.voiceList.constant.Constants;
import com.apurva.voiceList.database.executor.DBExecQueryExecutor;
import com.apurva.voiceList.database.table.ListItemTable;
import com.apurva.voiceList.database.table.ListTable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_LIST_TABLE_SQL;
    private static final String CREATE_LIST_ITEM_TABLE_SQL;

    private final static String CREATE_TABLE_QUERY_ID = "create table query";
    private final static String DROP_TABLE_QUERY_ID = "drop table query";

    static {
        String sql;
        sql = "CREATE TABLE IF NOT EXISTS " + ListTable.getTableName() + " ( ";
        sql += ListTable.LIST_ID.getColumnName() + " INTEGER NOT NULL, ";
        sql += ListTable.LIST_NAME.getColumnName() + " TEXT NOT NULL, ";
        sql += ListTable.CREATED_ON.getColumnName() + " TEXT NOT NULL, ";
        sql += ListTable.UPDATED_ON.getColumnName() + " TEXT NOT NULL, ";
        sql += "PRIMARY KEY(" + ListTable.LIST_ID.getColumnName() + ") );";
        CREATE_LIST_TABLE_SQL = sql;

        sql = "CREATE TABLE IF NOT EXISTS " + ListItemTable.getTableName() + " ( ";
        sql += ListItemTable.ITEM_ID.getColumnName() + " INTEGER NOT NULL, ";
        sql += ListItemTable.ITEM_NAME.getColumnName() + " TEXT NOT NULL, ";
        sql += ListItemTable.LIST_ID.getColumnName() + " INTEGER NOT NULL, ";
        sql += "PRIMARY KEY(" + ListItemTable.ITEM_ID.getColumnName() + "), ";
        sql += "FOREIGN KEY("+ ListItemTable.LIST_ID.getColumnName() + ") ";
        sql += "REFERENCES " + ListTable.getTableName() + "(" + ListTable.LIST_ID.getColumnName();
        sql += ") ON DELETE CASCADE );";
        CREATE_LIST_ITEM_TABLE_SQL = sql;
    }

    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            DBExecQueryExecutor executor = new DBExecQueryExecutor(CREATE_TABLE_QUERY_ID, db, null);
            executor.execute(CREATE_LIST_TABLE_SQL, CREATE_LIST_ITEM_TABLE_SQL);
        } catch(SQLiteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            dropTables(db);
            onCreate(db);
        } catch(SQLiteException e) {
            e.printStackTrace();
        }
    }

    private void dropTables(SQLiteDatabase db) {
        try {
            String sql1 = "DROP TABLE IF EXISTS " + ListTable.getTableName() + ";";
            String sql2 = "DROP TABLE IF EXISTS " + ListItemTable.getTableName() + ";";

            DBExecQueryExecutor executor = new DBExecQueryExecutor(DROP_TABLE_QUERY_ID, db, null);
            executor.execute(sql1, sql2);
        } catch(SQLiteException e) {
            e.printStackTrace();
        }
    }

}
