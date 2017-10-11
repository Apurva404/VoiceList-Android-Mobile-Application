package com.apurva.voiceList.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ListModel {
    private static String LIST_ID_JSON_KEY = "list id";
    private static String LIST_NAME_JSON_KEY = "item name";
    private static String LIST_CREATED_ON_JSON_KEY = "list created on";
    private static String LIST_UPDATED_ON_JSON_KEY = "list updated on";

    private String mListId;
    private String mListName;
    private String mCreatedOn;
    private String mUpdatedOn;

    public ListModel() {
        mListId = mListName = mCreatedOn = mUpdatedOn = null;
    }

    public ListModel(String id, String name, String createdOn, String updatedOn) {
        mListId = id;
        mListName = name;
        mCreatedOn = createdOn;
        mUpdatedOn = updatedOn;
    }

    public void setId(String s) { mListId = s; }
    public void setName(String s) { mListName = s; }
    public void setCreatedOn(String s) { mCreatedOn = s; }
    public void setmUpdatedOn(String s) { mUpdatedOn = s; }

    public String getId() { return mListId; }
    public String getName() { return mListName; }
    public String getCreatedOn() { return mCreatedOn; }
    public String getmUpdatedOn() { return mUpdatedOn; }

    public String serialize() {
        JSONObject json = new JSONObject();
        try {
            json.accumulate(LIST_ID_JSON_KEY, mListId);
            json.accumulate(LIST_NAME_JSON_KEY, mListName);
            json.accumulate(LIST_CREATED_ON_JSON_KEY, mCreatedOn);
            json.accumulate(LIST_UPDATED_ON_JSON_KEY, mCreatedOn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public static ListModel deserialize(String serializedObject) {
        ListModel object = new ListModel();
        try {
            JSONObject json = new JSONObject(serializedObject);
            object.mListId = json.getString(LIST_ID_JSON_KEY);
            object.mListName = json.getString(LIST_NAME_JSON_KEY);
            object.mCreatedOn = json.getString(LIST_CREATED_ON_JSON_KEY);
            object.mUpdatedOn = json.getString(LIST_UPDATED_ON_JSON_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
