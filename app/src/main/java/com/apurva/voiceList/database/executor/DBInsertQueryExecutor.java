package com.apurva.voiceList.database.executor;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.apurva.voiceList.database.resultInterface.InsertQueryResultHandler;

import java.util.Map;

public class DBInsertQueryExecutor extends AsyncTask<Map<String, ContentValues>, Void, Long> {
    private String mQueryId;
    private SQLiteDatabase mDb;
    private InsertQueryResultHandler mHandler;

    public DBInsertQueryExecutor(@NonNull String queryIdIn, @NonNull SQLiteDatabase dbIn, InsertQueryResultHandler handlerIn) {
        mQueryId = queryIdIn;
        mDb = dbIn;
        mHandler = handlerIn;
    }

    @Override
    protected Long doInBackground(Map<String, ContentValues>... params) {
        long result = -1;
        if(params.length == 1) {
            Map<String, ContentValues> param = params[0];
            if(param.size() == 1) {
                // hacky way to extract the only entry int the map
                for (Map.Entry<String, ContentValues> entry : param.entrySet()) {
                    result = mDb.insertOrThrow(entry.getKey(), null, entry.getValue());
                }
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Long result) {
        if(mHandler != null)
            mHandler.handleInsertQueryResult(mQueryId, result);
    }
}
