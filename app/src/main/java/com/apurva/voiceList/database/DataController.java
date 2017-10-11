package com.apurva.voiceList.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.apurva.voiceList.database.executor.DBExecQueryExecutor;
import com.apurva.voiceList.database.executor.DBInsertQueryExecutor;
import com.apurva.voiceList.database.executor.DBRawQueryExecutor;
import com.apurva.voiceList.database.resultInterface.ExecQueryResultHandler;
import com.apurva.voiceList.database.resultInterface.InsertQueryResultHandler;
import com.apurva.voiceList.database.resultInterface.RawQueryResultHandler;
import com.apurva.voiceList.database.table.ListItemTable;
import com.apurva.voiceList.database.table.ListTable;
import com.apurva.voiceList.model.ListItemModel;
import com.apurva.voiceList.model.ListModel;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataController implements RawQueryResultHandler, InsertQueryResultHandler, ExecQueryResultHandler {
    private DatabaseHelper mDBHelper;
    private Context mContext;
    private SQLiteDatabase mDB;

    public static final String SERIALIZED_LIST_MODEL_ARRAY_KEY = "array of serialized list model objects";
    public static final String SERIALIZED_LIST_ITEM_MODEL_ARRAY_KEY = "array of serialized list item model objects";
    public static final String INSERTED_LIST_ID_KEY = "list id";
    public static final String INSERTED_LIST_ITEM_ID_KEY = "list item id";

    public static final String INSERT_LIST_BROADCAST = "Insert List Result Broadcast";
    public static final String GET_FILTERED_LIST_BROADCAST = "Get Filtered Lists Result Broadcast";
    public static final String UPDATE_LIST_BROADCAST = "Update List Result Broadcast";
    public static final String DELETE_LIST_BROADCAST = "Delete List Result Broadcast";
    public static final String INSERT_ITEM_IN_LIST_BROADCAST = "Insert Item In List Result Broadcast";
    public static final String GET_ITEMS_IN_LIST_BROADCAST = "Get Items In List Result Broadcast";
    public static final String DELETE_ITEM_IN_LIST_BROADCAST = "Delete Item In List Result Broadcast";

    private static final String INSERT_LIST_QUERY_ID = "Insert List";
    private static final String GET_FILTERED_LIST_QUERY_ID = "Get Filtered Lists";
    private static final String UPDATE_LIST_QUERY_ID = "Insert List";
    private static final String DELETE_LIST_QUERY_ID = "Insert List";
    private static final String INSERT_ITEM_IN_LIST_QUERY_ID = "Insert Item In List";
    private static final String GET_ITEMS_IN_LIST_QUERY_ID = "Get Items In List";
    private static final String DELETE_ITEM_IN_LIST_QUERY_ID = "Insert Item In List";



    public DataController(@NonNull Context context) {
        this.mContext = context;
    }

    public DataController open() {
        mDBHelper = new DatabaseHelper(mContext);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }


    public void insertListAsync(String listName) {
        ContentValues content = new ContentValues();
        content.put(ListTable.LIST_NAME.getColumnName(), listName);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        content.put(ListTable.CREATED_ON.getColumnName(), currentDateTimeString);
        content.put(ListTable.UPDATED_ON.getColumnName(), currentDateTimeString);

        Map<String, ContentValues> param = new HashMap<>();
        param.put(ListTable.getTableName(), content);

        DBInsertQueryExecutor executor = new DBInsertQueryExecutor(INSERT_LIST_QUERY_ID, mDB, this);
        executor.execute(param);
    }

    public void updateListAsync(String inListId, String newListName) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String sql = "UPDATE " + ListTable.getTableName() + " SET ";
        sql += ListTable.LIST_NAME.getColumnName() + " = '" + newListName + "' , ";
        sql += ListTable.UPDATED_ON.getColumnName() + " = '" + currentDateTimeString + "' ";
        sql += "WHERE " + ListTable.LIST_ID.getColumnName() + " = " + inListId;

        DBExecQueryExecutor executor = new DBExecQueryExecutor(UPDATE_LIST_QUERY_ID, mDB, this);
        executor.execute(sql);
    }

    public void deleteListAsync(String listId) {
        String sql = "DELETE FROM " + ListTable.getTableName();
        sql += " WHERE " + ListTable.LIST_ID.getColumnName() + " = " + listId;

        DBExecQueryExecutor executor = new DBExecQueryExecutor(DELETE_LIST_QUERY_ID, mDB, this);
        executor.execute(sql);
    }

    public void getFilteredListsAsync(String filterText, String orderbyColumnName) {
        String sql = "SELECT * FROM " + ListTable.getTableName();
        sql += " WHERE " + ListTable.LIST_NAME.getColumnName() + " LIKE '%" + filterText + "%' ";
        sql += " ORDER BY " + orderbyColumnName;

        DBRawQueryExecutor executor = new DBRawQueryExecutor(GET_FILTERED_LIST_QUERY_ID, mDB, this);
        executor.execute(sql);
    }


    public void insertListItemAsync(String itemName, int listId) {
        ContentValues content=new ContentValues();

        content.put(ListItemTable.ITEM_NAME.getColumnName(), itemName);
        content.put(ListItemTable.LIST_ID.getColumnName(), listId);

        Map<String, ContentValues> param = new HashMap<>();
        param.put(ListItemTable.getTableName(), content);

        DBInsertQueryExecutor executor = new DBInsertQueryExecutor(INSERT_ITEM_IN_LIST_QUERY_ID, mDB, this);
        executor.execute(param);
    }

    public void deleteListItemAsync(String itemId) {
        String sql = "DELETE FROM " + ListItemTable.getTableName();
        sql += " WHERE " + ListItemTable.ITEM_ID.getColumnName() + " = " + itemId;

        DBExecQueryExecutor executor = new DBExecQueryExecutor(DELETE_ITEM_IN_LIST_QUERY_ID, mDB, this);
        executor.execute(sql);
    }

    public void getItemsInListAsync(String listId) {
        String sql = "SELECT * FROM " + ListItemTable.getTableName();
        sql += " WHERE " + ListItemTable.LIST_ID.getColumnName() + " = '" + listId + "';";

        DBRawQueryExecutor executor = new DBRawQueryExecutor(GET_ITEMS_IN_LIST_QUERY_ID, mDB, this);
        executor.execute(sql);
    }

    @Override
    public void handleRawQueryResult(String queryId, Cursor cursor) {
        ArrayList<String> result = new ArrayList<>();
        Intent intent = null;
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if(queryId.equals(GET_FILTERED_LIST_QUERY_ID)) {
                    int listId = cursor.getInt(ListTable.LIST_ID.getColumnIndex());
                    String listName = cursor.getString(ListTable.LIST_NAME.getColumnIndex());
                    String createdOn = cursor.getString(ListTable.CREATED_ON.getColumnIndex());
                    String updatedOn = cursor.getString(ListTable.UPDATED_ON.getColumnIndex());

                    ListModel lm = new ListModel(Integer.toString(listId), listName, createdOn, updatedOn);
                    result.add(lm.serialize());
                } else if(queryId.equals(GET_ITEMS_IN_LIST_QUERY_ID)) {
                    int listItemId = cursor.getInt(ListItemTable.ITEM_ID.getColumnIndex());
                    String listItemName = cursor.getString(ListItemTable.ITEM_NAME.getColumnIndex());
                    int listId = cursor.getInt(ListItemTable.LIST_ID.getColumnIndex());

                    ListItemModel lim = new ListItemModel(Integer.toString(listItemId), listItemName, Integer.toString(listId));
                    result.add(lim.serialize());
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        if(queryId.equals(GET_FILTERED_LIST_QUERY_ID)) {
            intent = new Intent(GET_FILTERED_LIST_BROADCAST);
            intent.putExtra(SERIALIZED_LIST_MODEL_ARRAY_KEY, result.toArray(new String[0]));
        } else if(queryId.equals(GET_ITEMS_IN_LIST_QUERY_ID)) {
            intent = new Intent(GET_ITEMS_IN_LIST_BROADCAST);
            intent.putExtra(SERIALIZED_LIST_ITEM_MODEL_ARRAY_KEY, result.toArray(new String[0]));
        }

        if(intent != null)
            mContext.sendBroadcast(intent);
    }

    @Override
    public void handleInsertQueryResult(String queryId, long result) {
        Intent intent = null;
        switch(queryId) {
            case INSERT_LIST_QUERY_ID:
                intent = new Intent(INSERT_LIST_BROADCAST);
                intent.putExtra(INSERTED_LIST_ID_KEY, Long.toString(result));
                break;
            case INSERT_ITEM_IN_LIST_QUERY_ID:
                intent = new Intent(INSERT_ITEM_IN_LIST_BROADCAST);
                intent.putExtra(INSERTED_LIST_ITEM_ID_KEY, Long.toString(result));
                break;
        }
        if(intent != null)
            mContext.sendBroadcast(intent);
    }

    @Override
    public void handleExecQueryResult(String queryId, boolean success) {
        Intent intent = null;
        switch(queryId) {
            case UPDATE_LIST_QUERY_ID:
                intent = new Intent(UPDATE_LIST_BROADCAST);
                break;
            case DELETE_LIST_BROADCAST:
                intent = new Intent(DELETE_LIST_BROADCAST);
                break;
            case DELETE_ITEM_IN_LIST_QUERY_ID:
                intent = new Intent(DELETE_ITEM_IN_LIST_BROADCAST);
                break;
        }
        if(intent != null)
            mContext.sendBroadcast(intent);
    }
}
