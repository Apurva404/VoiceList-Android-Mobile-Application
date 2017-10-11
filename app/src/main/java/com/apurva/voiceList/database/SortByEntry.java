package com.apurva.voiceList.database;

import com.apurva.voiceList.database.table.ListTable;

public enum SortByEntry {
    SORT_BY_NAME("Name", ListTable.LIST_NAME.getColumnName()),
    SORT_BY_CREATED_ON_DATE("Created on", ListTable.CREATED_ON.getColumnName()),
    SORT_BY_UPDATED_ON_DATE("Last updated", ListTable.UPDATED_ON.getColumnName());

    private String value;
    private String dbColumnName;

    SortByEntry(String s1, String s2) {
        value = s1;
        dbColumnName = s2;
    }
    public String toString() { return value; }
    public String getDbColumnName() { return dbColumnName; }
};
