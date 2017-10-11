package com.apurva.voiceList.database.table;


public enum ListItemTable {
    //public static final String LIST_ITEM_TABLE_COLUMN_NAME[] = { "itemId", "itemName", "listId" };
    ITEM_ID(0, "itemId"),
    ITEM_NAME(1, "itemName"),
    LIST_ID(2, "listId");

    private int mColumnIndex;
    private String mColumnName;
    private static int mColumnCount = values().length;
    private static String mTableName = "listItem";

    ListItemTable(int index, String name) {
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
