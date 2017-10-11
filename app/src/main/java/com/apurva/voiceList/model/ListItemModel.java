package com.apurva.voiceList.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ListItemModel {
    private static String ITEM_ID_JSON_KEY = "item id";
    private static String ITEM_NAME_JSON_KEY = "item name";
    private static String LIST_ID_JSON_KEY = "list id";

    private String mItemId;
    private String mItemName;
    private String mListId;

    public ListItemModel() {
        mItemId = mItemName = mListId = null;
    }

    public ListItemModel(String id, String name, String listId) {
        mItemId = id;
        mItemName = name;
        mListId = listId;
    }

    public void setId(String s) { mItemId = s; }
    public void setName(String s) { mItemName = s; }
    public void setListId(String s) { mListId = s; }

    public String getId() { return mItemId; }
    public String getName() { return mItemName; }
    public String getListId() { return mListId; }

    @Override
    public boolean equals(Object o) {
        if(o instanceof ListItemModel) {
            ListItemModel other = (ListItemModel)o;
            if((this.mItemId != null) && (other.mItemId.equals(this.mItemId)))
                return true;
            else {
                if((this.mListId == null) || (this.mListId.equals(other.mListId)))
                    return this.mItemName.equals(other.mItemName);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        if(this.mItemId != null)
            return Integer.valueOf(mItemId);
        else if(this.mItemName != null)
            return mItemName.hashCode();
        else if(this.mListId != null)
            return mListId.hashCode();
        else
            return 0;
    }

    public String serialize() {
        JSONObject json = new JSONObject();
        try {
            json.accumulate(ITEM_ID_JSON_KEY, mItemId);
            json.accumulate(ITEM_NAME_JSON_KEY, mItemName);
            json.accumulate(LIST_ID_JSON_KEY, mListId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public static ListItemModel deserialize(String serializedObject) {
        ListItemModel object = new ListItemModel();
        try {
            JSONObject json = new JSONObject(serializedObject);
            object.mItemId = json.getString(ITEM_ID_JSON_KEY);
            object.mItemName = json.getString(ITEM_NAME_JSON_KEY);
            object.mListId = json.getString(LIST_ID_JSON_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}