package com.apurva.voiceList.database.table;


public enum ListTable {
    //public static final String LIST_TABLE_COLUMN_NAME[] = { "listId", "listName", "createdOn", "updatedOn" };
    LIST_ID(0, "listId"),
    LIST_NAME(1, "listName"),
    CREATED_ON(2, "createdOn"),
    UPDATED_ON(3, "updatedOn");

    private int mColumnIndex;
    private String mColumnName;
    private static int mColumnCount = values().length;
    private static String mTableName = "list";

    ListTable(int index, String name) {
        mColumnIndex = index;
        mColumnName = name;
    }

    public int getColumnIndex() {
        return mColumnIndex;
    }

    public String getColumnName() {
        return mColumnName;
    }

    public static int getColumnCount() {
        return mColumnCount;
    }

    public static String getTableName() {
        return mTableName;
    }
}
