package com.apurva.voiceList.database.executor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.apurva.voiceList.database.resultInterface.RawQueryResultHandler;

public class DBRawQueryExecutor extends AsyncTask<String, Void, Cursor> {
    private String mId;
    private SQLiteDatabase mDb;
    private RawQueryResultHandler mHandler;


    public DBRawQueryExecutor(String idIn, SQLiteDatabase dbIn, RawQueryResultHandler handlerIn) {
        mId = idIn;
        mDb = dbIn;
        mHandler = handlerIn;
    }

    @Override
    protected Cursor doInBackground(String... params) {
        Cursor cursor = null;
        if(params.length > 0) {
            String sql = params[0];
            cursor = mDb.rawQuery(sql, null);
        }
        return cursor;
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        if(mHandler != null)
            mHandler.handleRawQueryResult(mId, cursor);
    }
}
