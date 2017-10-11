package com.apurva.voiceList.database.executor;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.apurva.voiceList.database.resultInterface.ExecQueryResultHandler;

public class DBExecQueryExecutor extends AsyncTask<String, Void, Boolean> {
    private String mQueryId;
    private SQLiteDatabase mDb;
    private ExecQueryResultHandler mHandler;

    public DBExecQueryExecutor(@NonNull String queryIdIn, @NonNull SQLiteDatabase dbIn, ExecQueryResultHandler handlerIn) {
        mQueryId = queryIdIn;
        mDb = dbIn;
        mHandler = handlerIn;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if(params.length > 0) {
            for(String sql : params) {
                mDb.execSQL(sql);
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(mHandler != null) {
            mHandler.handleExecQueryResult(mQueryId, result);
        }
    }

}
